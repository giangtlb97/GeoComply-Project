package com.example.geocomply;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

import android.util.Patterns;

import com.example.geocomply.utils.CommentParserUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MainUnitTest {
    Pattern mentionPattern = Pattern.compile("@(\\w+)");
    Pattern webPattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private MainViewModel mViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mViewModel = new MainViewModel();
    }

    @Test
    public void haveOneMention() {
        String comment ="@billgates is a billionaire";
        List<String> listComment = CommentParserUtil.extractByPattern(comment, mentionPattern);
        assertEquals(listComment.size(), 1);
        assertEquals(listComment.get(0), "@billgates");
    }

    @Test
    public void haveTwoMentions() {
        String comment ="@billgates and @mark are billionaire";
        List<String> listComment = CommentParserUtil.extractByPattern(comment, mentionPattern);
        assertEquals(listComment.size(), 2);
        assertEquals(listComment.get(0), "@billgates");
        assertEquals(listComment.get(1), "@mark");
    }

    @Test
    public void haveOneLink() {
        String comment ="Olympics 2020 is happening; https://olympics.com/tokyo-2020/en/";
        List<String> listComment = CommentParserUtil.extractByPattern(comment, webPattern);
        assertEquals(listComment.size(), 1);
        assertEquals(listComment.get(0), "https://olympics.com/tokyo-2020/en/");
    }

    @Test
    public void haveTwoLinks() {
        String comment ="Olympics 2020 is happening; https://olympics.com/tokyo-2020/en/ https://facebook.com";
        List<String> listComment = CommentParserUtil.extractByPattern(comment, webPattern);
        assertEquals(listComment.size(), 2);
        assertEquals(listComment.get(0), "https://olympics.com/tokyo-2020/en/");
        assertEquals(listComment.get(1), "https://facebook.com");
    }

    @Test
    public void haveOneLinkOneMention() {
        String comment ="@Giang Olympics 2020 is happening; https://olympics.com/tokyo-2020/en/";
        List<String> listLink = CommentParserUtil.extractByPattern(comment, webPattern);
        assertEquals(listLink.size(), 1);
        assertEquals(listLink.get(0), "https://olympics.com/tokyo-2020/en/");

        List<String> listMention = CommentParserUtil.extractByPattern(comment, mentionPattern);
        assertEquals(listMention.size(), 1);
        assertEquals(listMention.get(0), "@Giang");
    }

    @Test
    public void haveOneLink2() {
        String comment ="https://facebook.com";

        List<String> listLink = CommentParserUtil.extractByPattern(comment, webPattern);
        mViewModel.getTitleLink(listLink) ;
        assertEquals(mViewModel.linkResult, "title: Facebook\nurl:https://facebook.com");
    }





}