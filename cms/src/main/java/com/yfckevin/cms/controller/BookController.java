package com.yfckevin.cms.controller;

import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.VideoRequestDTO;
import com.yfckevin.common.enums.VisionType;
import com.yfckevin.cms.service.BookService;
import com.yfckevin.common.exception.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 呈現會員書櫃
     * @param memberId
     * @return
     */
    @GetMapping("/showBookcase/{memberId}")
    public ResponseEntity<?> showBookcase (@PathVariable String memberId){
        System.out.println("memberId = " + memberId);
        SearchDTO searchDTO = new SearchDTO();

        if (StringUtils.isNotBlank(memberId)) {
            searchDTO.setMemberId(memberId);
        } else {
            searchDTO.setVisionType(VisionType.Public.name());
        }

        if (StringUtils.isBlank(searchDTO.getKeyword())) {
            searchDTO.setKeyword("");
        }

        List<Book> bookList = bookService.findBook(searchDTO);
        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(bookList);
        return ResponseEntity.ok(resultStatus);
    }

    /**
     * 存多本書
     * @param memberId
     * @param imageRequestDTOs
     * @return
     */
    @PostMapping("/saveMultiBook")
    public ResponseEntity<ResultStatus> saveMultiBook(@RequestHeader("member-info") String memberId, @RequestBody List<ImageRequestDTO> imageRequestDTOs) {

        Map<String, Integer> savedBookStatusMap = bookService.saveMultiBook(imageRequestDTOs, memberId);

        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(savedBookStatusMap);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 刪書
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteBook/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id) {

        ResultStatus resultStatus = new ResultStatus();
        int count = bookService.deleteBook(id);

        if (count > 0) {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
        }

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 修書
     *
     * @return
     */
    @PostMapping("/editBook")
    public ResponseEntity<?> editBook(@RequestHeader("member-info") String memberId, @RequestBody BookDTO bookDTO) {

        ResultStatus resultStatus = new ResultStatus();

        int count = bookService.editBook(memberId, bookDTO);
        if (count > 0) {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        } else {
            resultStatus.setCode("C999");
            resultStatus.setMessage("例外發生");
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查書
     *
     * @param searchDTO
     * @return
     */
    @PostMapping("/searchBook")
    public ResponseEntity<?> searchBook(@RequestHeader("member-info") String memberId, @RequestBody SearchDTO searchDTO) {
        ResultStatus resultStatus = new ResultStatus();
        if (StringUtils.isBlank(searchDTO.getKeyword())) {
            searchDTO.setKeyword("");
        }
        searchDTO.setMemberId(memberId);
        List<Book> bookList = bookService.findBook(searchDTO);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(bookList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 取得影片Id
     * @param memberId
     * @param bookId
     * @return
     */
    @GetMapping("/getVideoId/{bookId}")
    public ResponseEntity<?> getVideoId(@RequestHeader("member-info") String memberId, @PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getVideoId(bookId, memberId));
    }


    /**
     * 取得書籍生成試閱影片的狀態
     * C001 (查無書籍)
     * C005 (尚未生成試閱影片)
     * C006 (查無影片)
     * C003 (製作影片過程有錯誤)
     * C004 (試閱影片製作中)
     * C000 (成功)
     * @param bookId
     * @return
     */
    @GetMapping("/previewBook/{bookId}")
    public ResponseEntity<?> previewBook (@PathVariable String bookId){
        return ResponseEntity.ok(bookService.previewBookStatus(bookId));
    }


    /**
     * 製作影片(旁白 -> 語音 -> 圖片 -> 影片)
     * @param dto
     * @return
     */
    @PostMapping("/constructVideo")
    public ResponseEntity<?> constructVideo(@RequestHeader("member-info") String memberId, @RequestBody VideoRequestDTO dto) {
        return ResponseEntity.ok(bookService.constructVideo(memberId, dto));
    }


    /**
     * 取得視聽影片的影片製作狀態
     * @param memberId
     * @return
     */
    @GetMapping("/getPreviewStatus")
    public ResponseEntity<?> getPreviewStatus (@RequestHeader("member-info") String memberId){
        return ResponseEntity.ok(bookService.getPreviewStatus(memberId));
    }
}
