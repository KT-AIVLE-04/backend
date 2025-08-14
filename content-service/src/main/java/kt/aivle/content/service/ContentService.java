package kt.aivle.content.service;

import kt.aivle.content.dto.ContentResponse;
import kt.aivle.content.dto.CreateContentRequest;

import java.io.IOException;

public interface ContentService {

    ContentResponse uploadContent(CreateContentRequest request) throws Exception;
}
