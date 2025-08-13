package kt.aivle.snspost.application.service;

import kt.aivle.snspost.adapter.in.web.dto.request.GenerateHashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.GeneratePostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.FullPostResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.PostResponse;
import kt.aivle.snspost.adapter.out.web.FastApiClient;
import kt.aivle.snspost.adapter.out.web.dto.*;
import kt.aivle.snspost.adapter.out.web.dto.FastApiSnsPostRequest;
import kt.aivle.snspost.adapter.out.web.dto.FastApiHashtagRequest;
import kt.aivle.snspost.application.port.in.SnsPostUseCase;
import kt.aivle.snspost.application.service.mapper.SnsPostMapper;
import kt.aivle.snspost.domain.model.Post;
import kt.aivle.snspost.domain.model.PostHashtag;
import kt.aivle.snspost.domain.model.Hashtag;
import kt.aivle.snspost.domain.model.SnsPlatform;
import kt.aivle.snspost.domain.model.ContentType;
import kt.aivle.snspost.application.port.out.PostRepository;
import kt.aivle.snspost.application.port.out.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsPostService implements SnsPostUseCase {

    private final FastApiClient fastApiClient;
    private final SnsPostMapper snsPostMapper;
    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;

    @Override
    @Transactional
    public Mono<PostResponse> generatePost(GeneratePostRequest request, Long userId, Long storeId) {
        FastApiSnsPostRequest fastApiRequest = snsPostMapper.toFastApiSnsPostRequest(request);
        
        return fastApiClient.generatePost(fastApiRequest)
                .flatMap(fastApiResponse -> {
                    // Post 엔티티 생성 및 저장
                    Post post = Post.builder()
                            .userId(userId)
                            .storeId(storeId)
                            .title(fastApiResponse.getTitle())
                            .content(fastApiResponse.getContent())
                            .location(request.getLocation())
                            .snsPlatform(request.getSnsPlatform())
                            .businessType(request.getBusinessType())
                            .contentData(request.getContentData())
                            .contentType(request.getContentType())
                            .userKeywords(String.join(",", request.getUserKeywords()))
                            .isPublic(true)
                            .build();
                    
                    Post savedPost = postRepository.save(post);
                    log.info("Post saved with ID: {}", savedPost.getId());
                    
                    return Mono.just(snsPostMapper.toPostResponse(fastApiResponse));
                });
    }

    @Override
    public Mono<HashtagResponse> generateHashtags(GenerateHashtagRequest request) {
        FastApiHashtagRequest fastApiRequest = snsPostMapper.toFastApiHashtagRequest(request);
        
        return fastApiClient.generateHashtags(fastApiRequest)
                .map(fastApiResponse -> snsPostMapper.toHashtagResponse(fastApiResponse));
    }

    @Override
    @Transactional
    public Mono<FullPostResponse> generateFullPost(GeneratePostRequest request, Long userId, Long storeId) {
        FastApiSnsPostRequest fastApiRequest = snsPostMapper.toFastApiSnsPostRequest(request);
        
        return fastApiClient.generateFullPost(fastApiRequest)
                .flatMap(fastApiResponse -> {
                    // Post 엔티티 생성 및 저장
                    Post post = Post.builder()
                            .userId(userId)
                            .storeId(storeId)
                            .title(fastApiResponse.getPost().getTitle())
                            .content(fastApiResponse.getPost().getContent())
                            .location(request.getLocation())
                            .snsPlatform(request.getSnsPlatform())
                            .businessType(request.getBusinessType())
                            .contentData(request.getContentData())
                            .contentType(request.getContentType())
                            .userKeywords(String.join(",", request.getUserKeywords()))
                            .isPublic(true)
                            .build();
                    
                    Post savedPost = postRepository.save(post);
                    log.info("Post saved with ID: {}", savedPost.getId());
                    
                    // 해시태그 처리
                    List<String> hashtagNames = fastApiResponse.getHashtags();
                    List<PostHashtag> postHashtags = hashtagNames.stream()
                            .map(hashtagName -> {
                                // 해시태그가 없으면 생성
                                Hashtag hashtag = hashtagRepository.findByName(hashtagName)
                                        .orElseGet(() -> {
                                            Hashtag newHashtag = Hashtag.builder().name(hashtagName).build();
                                            return hashtagRepository.save(newHashtag);
                                        });
                                
                                hashtag.incrementPostCount();
                                hashtagRepository.save(hashtag);
                                
                                return PostHashtag.builder()
                                        .post(savedPost)
                                        .hashtag(hashtag)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    // PostHashtag 저장
                    postHashtags.forEach(postHashtag -> {
                        savedPost.addHashtag(postHashtag);
                    });
                    
                    postRepository.save(savedPost);
                    log.info("Post hashtags saved for post ID: {}", savedPost.getId());
                    
                    return Mono.just(snsPostMapper.toFullPostResponse(fastApiResponse));
                });
    }
} 