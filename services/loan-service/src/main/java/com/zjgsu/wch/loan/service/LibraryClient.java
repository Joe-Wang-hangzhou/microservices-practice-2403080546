package com.zjgsu.wch.loan.service;
import com.zjgsu.wch.loan.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
@Component
public class LibraryClient {
    private final RestTemplate http;
    private final String readerUrl,bookUrl;
    public record ReaderView(Long id,String name,boolean active) {}
    public record CopyView(Long id,Long bookId,boolean borrowed) {}
    public LibraryClient(@Value("${dependencies.reader-url}") String readerUrl,
                         @Value("${dependencies.book-url}") String bookUrl,
                         @Value("${dependencies.connect-timeout-ms:2000}") int connectTimeout,
                         @Value("${dependencies.read-timeout-ms:3000}") int readTimeout) {
        this.readerUrl=readerUrl; this.bookUrl=bookUrl;
        SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); factory.setReadTimeout(readTimeout);
        this.http=new RestTemplate(factory);
    }
    public void validateReader(Long id) {
        ReaderView reader=request(readerUrl+"/api/readers/"+id,HttpMethod.GET,ReaderView.class);
        if(reader==null || !reader.active()) throw new BusinessException(HttpStatus.CONFLICT,"读者不可借阅");
    }
    public void borrowCopy(Long id) { request(bookUrl+"/api/copies/"+id+"/borrow",HttpMethod.POST,CopyView.class); }
    public void returnCopy(Long id) { request(bookUrl+"/api/copies/"+id+"/return",HttpMethod.POST,CopyView.class); }
    private <T> T request(String url,HttpMethod method,Class<T> type) {
        try { return http.exchange(url,method,HttpEntity.EMPTY,type).getBody(); }
        catch(HttpClientErrorException ex) {
            if(ex.getStatusCode().value()==404) throw new BusinessException(HttpStatus.NOT_FOUND,"依赖资源不存在");
            if(ex.getStatusCode().value()==409) throw new BusinessException(HttpStatus.CONFLICT,"依赖资源状态冲突");
            throw new BusinessException(HttpStatus.BAD_GATEWAY,"依赖服务拒绝请求");
        } catch(RestClientException ex) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,"依赖服务不可用或调用超时");
        }
    }
}
