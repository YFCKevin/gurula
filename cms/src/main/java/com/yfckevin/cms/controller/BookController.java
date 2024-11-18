package com.yfckevin.cms.controller;

import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.enums.VisionType;
import com.yfckevin.cms.service.BookService;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/showBookcase")
    public ResponseEntity<?> showBookcase (@RequestParam(required = false) String memberId){
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


    @PostMapping("/saveMultiBook")
    public ResponseEntity<ResultStatus> saveMultiBook(@RequestBody List<ImageRequestDTO> imageRequestDTOs) {

        final String memberId = MemberContext.getMember();
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
    public ResponseEntity<?> editBook(@RequestBody BookDTO bookDTO) {

        ResultStatus resultStatus = new ResultStatus();

        int count = bookService.editBook(bookDTO);
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
    public ResponseEntity<?> searchBook(@RequestBody SearchDTO searchDTO) {
        ResultStatus resultStatus = new ResultStatus();
        if (StringUtils.isBlank(searchDTO.getKeyword())) {
            searchDTO.setKeyword("");
        }
        List<Book> bookList = bookService.findBook(searchDTO);
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(bookList);
        return ResponseEntity.ok(resultStatus);
    }
}
