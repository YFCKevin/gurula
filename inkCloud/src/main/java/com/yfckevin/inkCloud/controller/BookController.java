package com.yfckevin.inkCloud.controller;

import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.api.inkCloud.BookApi;
import com.yfckevin.api.api.member.MemberApi;
import com.yfckevin.api.dto.inkCloud.BookResponseDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.common.dto.inkCloud.VideoRequestDTO;
import com.yfckevin.common.dto.member.MemberDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BookController {

    private final BookApi bookApi;
    private final MemberApi memberApi;

    public BookController(BookApi bookApi, MemberApi memberApi) {
        this.bookApi = bookApi;
        this.memberApi = memberApi;
    }

    /**
     * 取得當前會員的id與名稱，用來顯示登入登出以及顯示該會員書櫃之用途
     * @return
     */
    @GetMapping("/memberInfo")
    public ResponseEntity<?> memberInfo () {
        Map<String, String> dataMap = new HashMap<>();
        final String memberId = MemberContext.getMember();
        System.out.println("一朵墨取得memberId = " + memberId);

        if (StringUtils.isNotBlank(memberId)) {
            final ForestResponse<ResultStatus<MemberDTO>> response = memberApi.getMemberInfo(memberId);
            final String responseCode = response.getResult().getCode();
            final String message = response.getResult().getMessage();
            final MemberDTO memberDTO = response.getResult().getData();
            if ("C000".equals(responseCode)) {
                dataMap.put("memberId", memberId);
                dataMap.put("memberName", memberDTO.getName());
            }
        } else {
            dataMap.put("memberId", null);
            dataMap.put("memberName", null);
        }

        return ResponseEntity.ok(dataMap);
    }


    /**
     * 登出
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout (){
        ResultStatus resultStatus = new ResultStatus();
        final String memberId = MemberContext.getMember();
        System.out.println(memberId + " [使用者登出]");
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 展示會員書櫃
     * @param memberId
     * @return
     */
    @GetMapping("/bookcase/{memberId}")
    public ResponseEntity<?> bookcase(@PathVariable(required = false) String memberId) {

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


    /**
     * 取得影片Id
     * @param bookId
     * @return
     */
    @GetMapping("/getVideoId/{bookId}")
    public ResponseEntity<?> getVideoId(@PathVariable String bookId) {
        ResultStatus resultStatus = new ResultStatus();
        final ForestResponse<ResultStatus<String>> response = bookApi.getVideoId(bookId);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final String videoId = response.getResult().getData();
        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        resultStatus.setData(videoId);
        return ResponseEntity.ok(resultStatus);
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
        ResultStatus resultStatus = new ResultStatus();
        final ForestResponse<ResultStatus<String>> response = bookApi.previewBook(bookId);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final String videoUrl = response.getResult().getData();
        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        resultStatus.setData(videoUrl);
        return ResponseEntity.ok(resultStatus);
    }



    /**
     * 製作影片(旁白 -> 語音 -> 圖片 -> 影片)
     * @param dto
     * @return
     */
    @PostMapping("/constructVideo")
    public ResponseEntity<?> constructVideo(@RequestBody VideoRequestDTO dto) {
        ResultStatus resultStatus = new ResultStatus();
        final ForestResponse<ResultStatus<?>> response = bookApi.constructVideo(dto);
        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 取得視聽影片的影片製作狀態
     * @return
     */
    @GetMapping("/getPreviewStatus")
    public ResponseEntity<?> getPreviewStatus (){

        ResultStatus resultStatus = new ResultStatus();

        final ForestResponse<ResultStatus<List<String>>> response = bookApi.getPreviewStatus();

        final String responseCode = response.getResult().getCode();
        final String responseMessage = response.getResult().getMessage();
        final List<String> inProcessBookIds = response.getResult().getData();
        resultStatus.setCode(responseCode);
        resultStatus.setMessage(responseMessage);
        resultStatus.setData(inProcessBookIds);
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
