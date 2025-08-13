package kt.aivle.snspost.adapter.out.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiFullPostResponse {

    private FastApiPostResponse post;
    private List<String> hashtags;
} 