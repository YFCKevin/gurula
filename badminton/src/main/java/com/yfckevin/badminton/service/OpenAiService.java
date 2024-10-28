package com.yfckevin.badminton.service;

import com.yfckevin.badminton.entity.Post;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OpenAiService {
    CompletableFuture<List<Post>> generatePosts(String prompt) throws Exception;
}
