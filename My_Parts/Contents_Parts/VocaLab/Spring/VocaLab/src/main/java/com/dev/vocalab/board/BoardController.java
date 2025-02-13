package com.dev.vocalab.board;

import com.dev.vocalab.files.FilesRepository;
import com.dev.vocalab.files.FilesService;
import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.users.details.CustomUsersDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/CS")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final FilesService filesService;
    private final UsersRepository usersRepository;
    private final String realPath = "C:/Dev/Data/VocaLab/Spring/VocaLab/src/main/resources/static/images/upload";
    private final BoardRepository boardRepository;
    private final FilesRepository filesRepository;

    /*
    페이지로 이동되는 메서드들
    */
//    CS/Main, /CS , /CS/ 를 공지사항으로 이동시킵니다.
    @GetMapping({"/Main", "","/"})
    public String boardHome() {
        return "redirect:/CS/Notice";
    }

//    공지사항 탭 입니다.
    @GetMapping("/Notice")
    public String boardNotice(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(required = false) String searchWord,
                              Model model,
                              HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.NOTICE, "Notice", "공지사항",
                pageNum, searchWord, model, request);
    }

//    FAQ 탭 입니다.
    @GetMapping("/FAQ")
    public String boardFaq(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(required = false) String searchWord,
                           Model model,
                           HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.FAQ, "FAQ", "자주 묻는 질문",
                pageNum, searchWord, model, request);
    }

//    1:1 상담 탭 입니다.
    @GetMapping("/Inquiry")
    public String boardInquiry(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(required = false) String searchWord,
                               Model model,
                               HttpServletRequest request) {
        // SecurityContext에서 인증 정보 가져오기
        AuthenticationUtil.addUserSessionToModel(model);
        return handleBoardList(BoardEntity.Category.INQUIRY, "Inquiry", "1:1 문의",
                pageNum, searchWord, model, request);
    }
//    게시글 작성 뷰 페이지 이동입니다.
    @GetMapping("/Write")
    public String write(Model model) {
//        로그인 되어 있지 않으면 작성 불가
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);
        filesService.deleteFolder(realPath + "/board/temp/");
        return "board/boardWrite";
    }
@GetMapping("/View")
public String view(Model model, @RequestParam Integer boardId) {
    // 사용자 정보를 세션에 추가
    AuthenticationUtil.addUserSessionToModel(model);

    String userId = AuthenticationUtil.getCurrentUserId();
    CustomUsersDetails.UserRole userRole = AuthenticationUtil.getCurrentUserRole();

    // 게시글 가져오기
    return boardService.findBoardWithUserNickname(boardId).map(dto -> {
        // 게시글의 카테고리 확인
        if (dto.getCategory() == BoardEntity.Category.INQUIRY) {
            // Inquiry 게시판 접근 제한
            if (userId == null ||
                    !(userRole == CustomUsersDetails.UserRole.ADMIN || userId.equals(dto.getUserId()))) {
                return "redirect:/CS/Main"; // 권한 없음 처리
            }
        } else if (dto.getCategory() == BoardEntity.Category.REPLY) {
            return "redirect:/CS/Main";// Reply 카테고리는 항상 접근 금지
        }

        // Notice나 FAQ는 로그인 여부와 상관없이 접근 가능
        // 게시글 내용 처리 및 View 반환
        dto.setContent(dto.getContent().replaceAll("\r\n", "<br>"));
        model.addAttribute("row", dto);
        return "board/boardView";
    }).orElseGet(() -> {// 게시글이 존재하지 않는 경우 처리
        model.addAttribute("row", null);
        model.addAttribute("userNickname", "알 수 없음");
        return "redirect:/CS/Main";
    });
}
    //    게시글 수정 페이지 이동입니다.
    @GetMapping("/Edit")
    public String boardEdit(Model model, @RequestParam Integer boardId) {
        // 현재 로그인된 사용자 가져오기
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String currentUserId = AuthenticationUtil.getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 현재 유저가 게시글 작성자인지 확인
        if (!currentUserId.equals(board.getUser().getUserId())) {
            return "redirect:/CS/Notice";
        }
        String tempPath = realPath + "/board/temp/";
        filesService.deleteFolder(tempPath);
        String originPath = realPath + "/board/" + board.getBoardId() + "/";
        filesService.copyFiles(originPath, tempPath);
        board.setContent(board.getContent().replaceAll("/" + board.getBoardId() + "/", "/temp/"));
        model.addAttribute("row", board);
        return "board/boardEdit";
    }
//    1:1 문의 답변 입니다.
    @GetMapping("/Reply")
    @ResponseBody
    public ResponseEntity<BoardDTO> getReplyBoard(@RequestParam Integer parentId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            BoardDTO replyBoard = boardService.findReplyByParentId(parentId);
            if (replyBoard != null) {
                // 줄바꿈 처리
                replyBoard.setContent(replyBoard.getContent().replaceAll("\r\n", "<br>"));
                return ResponseEntity.ok(replyBoard);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("답변 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
    CRUD 메서드들
    */
    @PostMapping("/Post")
    public String boardPosting(@RequestParam String title,
                               @RequestParam String content,
                               @RequestParam BoardEntity.Category category,
                               @RequestParam String replyStatus,
                               @RequestParam(required = false) MultipartFile thumbnail,
                               Integer parentId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        UsersEntity user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (parentId != null) {
            BoardEntity parentBoard = boardRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 게시글을 찾을 수 없습니다."));
            parentBoard.setReplyStatus(BoardEntity.ReplyStatus.DONE);
            boardRepository.save(parentBoard);
        }
        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .replyStatus(BoardEntity.ReplyStatus.valueOf(replyStatus))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentId(parentId)
                .build();
        BoardEntity savedBoard = boardService.savePost(board, null, null);
        filesService.handleBoardFileUpload(savedBoard.getBoardId(), thumbnail, userId);
        return "redirect:/CS/Notice";
    }

    @PostMapping("/Update")
    @Transactional
    public String boardUpdate(@RequestParam Integer boardId,
                              @RequestParam String title,
                              @RequestParam String content,
                              @RequestParam String category,
                              @RequestParam String replyStatus,
                              @RequestParam(required = false) MultipartFile thumbnail) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String currentUserId = AuthenticationUtil.getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }
        // 게시글 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 작성자 본인 확인
        if (!currentUserId.equals(board.getUser().getUserId())) {
            return "redirect:/CS/Notice";
        }
        // 게시글 업데이트
        filesService.handleBoardFileUpdate(boardId, content, thumbnail, currentUserId);
        board.setTitle(title);
        board.setContent(content.replaceAll("/temp/", "/" + boardId + "/"));
        board.setCategory(BoardEntity.Category.valueOf(category));
        board.setReplyStatus(BoardEntity.ReplyStatus.valueOf(replyStatus));
        board.setUpdatedAt(LocalDateTime.now());
        boardService.savePost(board, null, null);
        return "redirect:/CS/View?boardId=" + boardId;
    }

    @PostMapping("/Delete")
    public String boardDelete(@RequestParam Integer boardId) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String currentUserId = AuthenticationUtil.getCurrentUserId();
        CustomUsersDetails.UserRole userRole = AuthenticationUtil.getCurrentUserRole();
        if (currentUserId == null) {
            return "redirect:/login";
        }
        // 게시글 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 작성자 본인 또는 관리자만 삭제 가능
        if (!(currentUserId.equals(board.getUser().getUserId()) ||
                userRole == CustomUsersDetails.UserRole.ADMIN)) {
            return "redirect:/CS/Notice";
        }
        String category = board.getCategory().toString();
        // 답변글이 있는지 확인
        Optional<BoardEntity> replyBoard = boardRepository.findByParentId(boardId);

        // 답변글이 있다면 먼저 삭제
        if (replyBoard.isPresent()) {
            Integer replyBoardId = replyBoard.get().getBoardId();
            filesService.deleteAllBoardFiles(replyBoardId);
            filesService.deleteFolder(realPath + "/board/" + replyBoardId);
            boardRepository.delete(replyBoard.get());
        }
        // 원본 게시글 삭제
        boardService.deleteBoard(boardId);
        filesService.deleteFolder(realPath + "/board/" + boardId);
        // 카테고리 값 변환
        String redirectCategory = category.equals("FAQ") ?
                category :
                String.valueOf(category.charAt(0)) + category.substring(1).toLowerCase();

        return "redirect:/CS/" + redirectCategory;
    }

    /*
    내부 Helper 메서드들
    */
    private String handleBoardList(BoardEntity.Category category, String boardCat,
                                   String pageTitle, int pageNum, String searchWord,
                                   Model model, HttpServletRequest request) {
        request.setAttribute("pageNum", pageNum);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticationUtil.addUserSessionToModel(model);

        Page<BoardDTO> boardList;
        Pageable pageable = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Order.desc("boardId")));

        if (category == BoardEntity.Category.INQUIRY) {
            // INQUIRY 카테고리 처리 로직 분리
            boardList = handleInquiryCategory(searchWord, pageable);
        } else {
            // INQUIRY가 아닌 경우 기존 로직 유지
            if (searchWord == null || searchWord.isEmpty()) {
                boardList = boardService.selectListPage(pageable, category);
            } else {
                searchWord = searchWord.trim();
                boardList = boardService.selectListPageSearch("%" + searchWord + "%", pageable, category);
            }
        }
        model.addAttribute("boardList", boardList);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("boardCat", boardCat);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("pagingImg",
                PagingUtil.pagingImg(
                        (int) boardList.getTotalElements(),
                        boardList.getSize(),
                        5,
                        pageNum,
                        request.getContextPath() + "/CS/" + boardCat + "?",
                        searchWord
                )
        );

        return "board/boardList";
    }
//    1:1문의 탭 조건에 따라 분류 나눔
    private Page<BoardDTO> handleInquiryCategory(String searchWord, Pageable pageable) {
        if (!AuthenticationUtil.isAuthenticated()) {
            return Page.empty(pageable);
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        CustomUsersDetails.UserRole userRole = AuthenticationUtil.getCurrentUserRole();
        if (userId == null) {
            return Page.empty(pageable);
        }
        // ADMIN 사용자는 모든 게시글 조회 가능
        if (userRole == CustomUsersDetails.UserRole.ADMIN) {
            if (searchWord == null || searchWord.isEmpty()) {
                return boardService.selectListPage(pageable, BoardEntity.Category.INQUIRY);
            }
            return boardService.selectListPageSearch("%" + searchWord.trim() + "%", pageable, BoardEntity.Category.INQUIRY);
        }
        // 일반 사용자는 자신의 게시글만 조회
        if (searchWord == null || searchWord.isEmpty()) {
            return boardService.selectListPageByUserId(pageable, BoardEntity.Category.INQUIRY, userId);
        }
        return boardService.selectListPageByUserIdAndSearch("%" + searchWord.trim() + "%", pageable, BoardEntity.Category.INQUIRY, userId);
    }
    @PostMapping(value = "/uploadSummernoteImageFile", produces = "application/json; charset=utf8")
    @ResponseBody
    public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        log.info("이미지 업로드 요청: {}", multipartFile.getOriginalFilename());
        String tempPath = realPath + "/board/temp/";
        return filesService.uploadFile(multipartFile, tempPath).toString();
    }
}
