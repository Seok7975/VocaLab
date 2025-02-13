package com.dev.vocalab.board;

import com.dev.vocalab.files.FilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BoardService {
    private final BoardRepository boardRepository;
    private final FilesService filesService;

    @Autowired
    public BoardService(BoardRepository boardRepository, FilesService filesService) {
        this.boardRepository = boardRepository;
        this.filesService = filesService;
    }

    // 전체 리스트 불러오기
    public Page<BoardDTO> selectListPage(Pageable pageable, BoardEntity.Category category) {
        return boardRepository.findByCategory(category, pageable)
                .map(this::convertToDTO);
    }

    // 검색 조건에 따른 페이지 리스트 불러오기
    public Page<BoardDTO> selectListPageSearch(String search, Pageable pageable, BoardEntity.Category category) {
        // 검색어를 안전하게 처리
        String safeSearch = (search == null || search.isBlank()) ? "" : search.trim();
        return boardRepository.findByTitleLikeAndCategory(safeSearch, category, pageable)
                .map(this::convertToDTO);
    }
    // parentId로 답변 게시글 조회
    public BoardDTO findReplyByParentId(Integer parentId) {
        return boardRepository.findByParentId(parentId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public Page<BoardDTO> selectListPageByUserId(Pageable pageable, BoardEntity.Category category, String userId) {
        return boardRepository.findAllByCategoryAndUserId(category, userId, pageable)
                .map(this::convertToDTO);
    }

    public Page<BoardDTO> selectListPageByUserIdAndSearch(String searchWord, Pageable pageable, BoardEntity.Category category, String userId) {
        // 검색어를 안전하게 처리
        String safeSearchWord = (searchWord == null || searchWord.isBlank()) ? "" : searchWord.trim();
        return boardRepository.findAllByCategoryAndUserIdAndSearch(safeSearchWord, pageable, category, userId)
                .map(this::convertToDTO);
    }



    // 게시글 작성/수정
    @Transactional
    public BoardEntity savePost(BoardEntity board, String oldContent, String newContent) {
        validateBoard(board);

        // 1. 게시글 먼저 저장하여 ID 받기
        BoardEntity savedBoard = boardRepository.save(board);

        // 2. ID를 이용하여 content 내 이미지 경로 업데이트
        if (oldContent != null && newContent != null && !oldContent.equals(newContent)) {
            savedBoard.setContent(newContent);
        } else if (board.getContent() != null && board.getContent().contains("/temp")) {
            // temp 경로를 실제 boardId로 변경
            String updatedContent = board.getContent().replaceAll(
                    "/board/temp",
                    "/board/" + savedBoard.getBoardId()
            );
            savedBoard.setContent(updatedContent);
        }

        // 3. 변경된 내용으로 다시 저장
        return boardRepository.save(savedBoard);
    }

    // 게시글 조회
    @Transactional(readOnly = true)
    public Optional<BoardDTO> findBoardWithUserNickname(Integer boardId) {
        // 이미 DTO로 변환된 결과를 바로 반환
        return boardRepository.findBoardWithUserNicknameByBoardId(boardId);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Integer boardId) {
        boardRepository.findById(boardId).ifPresent(board -> {
            filesService.deleteAllBoardFiles(boardId);
            boardRepository.delete(board);
        });
    }

    // DTO 변환
    private BoardDTO convertToDTO(BoardEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        return BoardDTO.builder()
                .boardId(entity.getBoardId())
                .category(entity.getCategory())
                .title(entity.getTitle())
                .content(entity.getContent())
                .replyStatus(entity.getReplyStatus() != null ? entity.getReplyStatus() : BoardEntity.ReplyStatus.NONE)
                .userId(entity.getUser()!= null ? entity.getUser().getUserId() : "Unknown")
                .userNickname(entity.getUser() != null ? entity.getUser().getUserNickname() : "Unknown")
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // 게시글 유효성 검증
    private void validateBoard(BoardEntity board) {
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (board.getCategory() == null) {
            throw new IllegalArgumentException("카테고리를 선택해주세요.");
        }
    }
}