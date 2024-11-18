package com.yfckevin.inkCloud.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.api.inkCloud.BookApi;
import com.yfckevin.api.badminton.dto.inkCloud.BookResponseDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class BookController {

    private final BookApi bookApi;

    public BookController(BookApi bookApi) {
        this.bookApi = bookApi;
    }

    @GetMapping("/bookcase")
    public ResponseEntity<?> bookcase() {

        final String memberId = MemberContext.getMember();

        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<BookResponseDTO>>> response = bookApi.showBookcase(memberId);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final List<BookResponseDTO> bookData = response.getResult().getData();

        if ("C000".equals(responseCode)) {
            List<BookDTO> bookDTOList = bookData.stream()
                    .map(this::constructBookDTO)
                    .sorted(Comparator.comparing(BookDTO::getCreationDate).reversed())
                    .collect(Collectors.toList());

            resultStatus.setData(bookDTOList);
        }
        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 批量存書
     *
     * @param imageRequestDTOs
     * @return
     */
    @PostMapping("/saveMultiBook")
    public ResponseEntity<?> saveMultiBook(@RequestBody List<ImageRequestDTO> imageRequestDTOs) {
        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<Map<String, Integer>>> response = bookApi.saveMultiBook(imageRequestDTOs);

        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final Map<String, Integer> savedBookStatus = response.getResult().getData();

        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        resultStatus.setData(savedBookStatus);
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

        ForestResponse<ResultStatus<?>> response = bookApi.deleteBook(id);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();

        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
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

        ForestResponse<ResultStatus<?>> response = bookApi.editBook(bookDTO);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();

        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
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

        ForestResponse<ResultStatus<List<BookResponseDTO>>> response = bookApi.searchBook(searchDTO);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final List<BookResponseDTO> bookList = response.getResult().getData();

        final List<BookDTO> bookDTOList = bookList.stream()
                .map(this::constructBookDTO)
                .sorted(Comparator.comparing(BookDTO::getCreationDate).reversed())
                .collect(Collectors.toList());

        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        resultStatus.setData(bookDTOList);
        return ResponseEntity.ok(resultStatus);
    }


    private BookDTO constructBookDTO(BookResponseDTO book) {
        BookDTO dto = new BookDTO();
        dto.setTitle(book.getTitle());
        dto.setId(book.getId());
        dto.setPublisher(book.getPublisher());
        dto.setAuthor(book.getAuthor());
        dto.setCreationDate(book.getCreationDate());
        dto.setCreator(book.getCreator());
        dto.setModificationDate(book.getModificationDate());
        dto.setModifier(book.getModifier());
        return dto;
    }
}
