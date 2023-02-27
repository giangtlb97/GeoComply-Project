package com.example.geocomply;

import android.util.Pair;
import android.util.Patterns;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.geocomply.utils.CommentParserUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        StringBuilder linkInfoBuilder = new StringBuilder();
        compositeDisposable.add(
                getData(listUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> {
                            linkInfoBuilder.append("title: " + data.first + "\n");
                            linkInfoBuilder.append("url: " + data.second + "\n\n");
                        }, error -> {
                            isLoading.postValue(false);
                            textCommentError.postValue("Invalid url");
                        }, () -> {
                            isLoading.postValue(false);
                            linkResult.postValue(linkInfoBuilder.toString());
                        })
        );
    }

    Observable<Pair<String, String>> getData(List<String> listUrl) {
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
                    return Observable.just(new Pair<>(title, absoluteUrl));
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
            StringBuilder mentionsResult = new StringBuilder();

            mentionsList = CommentParserUtil.extractByPattern(textComment.getValue(), mentionPattern);
            if (!mentionsList.isEmpty()) {
                mentionsList.forEach(e -> mentionsResult.append(e.replace("@", "") + "\n"));
                mentions.setValue(mentionsResult.toString());
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
