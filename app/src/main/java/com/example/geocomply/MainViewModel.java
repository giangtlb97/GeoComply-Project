package com.example.geocomply;

import static java.util.stream.Collectors.toList;

import android.util.Pair;
import android.util.Patterns;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.geocomply.utils.CommentParserUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends ViewModel {
    public MutableLiveData<String> textComment = new MutableLiveData<>();
    public MutableLiveData<String> textCommentError = new MutableLiveData<>();
    public MutableLiveData<String> mentions = new MutableLiveData<>();
    public MutableLiveData<String> linkResult = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public List<String> mentionsList = new ArrayList<>();
    public List<String> listUrl = new ArrayList<>();

    Pattern mentionPattern = Pattern.compile("@(\\w+)");
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MainViewModel() {

    }

    void getTitleLink(List<String> listUrl) {
        List<LinkInfo> listLinkInfo = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        compositeDisposable.add(
                getData(listUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listLinkInfo::add, error -> {
                            isLoading.postValue(false);
                            textCommentError.postValue("Invalid url");
                        }, () -> {
                            isLoading.postValue(false);
                            linkResult.postValue(gson.toJson(listLinkInfo));
                        })
        );
    }

    Observable<LinkInfo> getData(List<String> listUrl) {
        return Observable.fromIterable(listUrl)
                .concatMap(url -> {
                    String absoluteUrl = "";
                    if (url.startsWith("http://") | url.startsWith("https://")) {
                        absoluteUrl = url;
                    } else {
                        absoluteUrl = "https://" + url;
                    }
                    Document doc = Jsoup.connect(absoluteUrl).get();
                    String title = doc.title();
                    return Observable.just(new LinkInfo(title, absoluteUrl));
                });
    }


    public void onButtonClicked() {
        isLoading.setValue(true);
        textCommentError.setValue(null);
        textComment.getValue();

        if (textComment.getValue() == null || textComment.getValue().isEmpty()) {
            textCommentError.setValue("Please enter your comment");
            mentions.setValue("No mention");
            linkResult.setValue("No link detected");
            isLoading.setValue(false);
        } else {
            // detect mentions
            List<String> mentionsResult = new ArrayList<>();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            mentionsList = CommentParserUtil.extractByPattern(textComment.getValue(), mentionPattern);
            if (!mentionsList.isEmpty()) {
                mentionsResult = mentionsList.stream().map(o -> o.replace("@", "")).collect(toList());
                mentions.setValue(gson.toJson(mentionsResult));
            } else {
                mentions.setValue("No mention");
            }

            //detect link
            listUrl = CommentParserUtil.extractByPattern(textComment.getValue(), Patterns.WEB_URL);
            if (listUrl.isEmpty()) {
                linkResult.setValue("No link detected");
                isLoading.setValue(false);
            } else getTitleLink(listUrl);

        }
    }

}
