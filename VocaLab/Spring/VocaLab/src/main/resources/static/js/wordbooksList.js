// 상수 정의
const MESSAGES = {
    DELETE_CONFIRM: (title) => `단어장 "${title}"을(를) 삭제하시겠습니까?`,
    DELETE_SUCCESS: (title) => `단어장 "${title}"이(가) 삭제되었습니다.`,
    FAVORITE_TOGGLE: (title, action) => `단어장 "${title}"의 즐겨찾기 상태가 변경되었습니다.`,
    PAGE_MOVE: (page, title) => `${page} 페이지로 이동합니다. 단어장: "${title}"`
};

class WordbookManager {
    constructor() {
        this.selectedWordBook = null;
        this.initializeEventListeners();
    }

    // 모달 UI 업데이트
    updateModalUI() {
        if (this.selectedWordBook && this.selectedWordBook.title) {
            console.log('Updating modal UI with:', this.selectedWordBook);
            const modalTitle = this.selectedWordBook.title;
            $('#modalWordbookTitle').text(modalTitle);
            const bookmarkText = this.selectedWordBook.bookmark ? '즐겨찾기 해제' : '즐겨찾기 등록';
            $('#btnToggleFavorite').text(bookmarkText);
        }
    }

    // 모달 이벤트 바인딩
    bindModalEvents() {
        $('#btnMoveWordbook').on('click', () => this.handleWordbookMove());
        $('#btnDeleteWordbook').on('click', () => this.handleWordbookDelete());
        $('#btnToggleFavorite').on('click', () => this.handleFavoriteToggle());
        $('.modal-body').on('click', '[id^=btnGo]', (e) => this.handlePageNavigation(e));
        $('#btnEditTitle').on('click', () => this.handleTitleEdit());
    }

    // 단어장 이동 처리
    handleWordbookMove() {
        if (this.selectedWordBook) {
            window.location.href = '/WordBook/Word?wordBookId=' + this.selectedWordBook.id;
        }
    }

    // 단어장 삭제 처리
    handleWordbookDelete() {
        const selectedWordBook = this.selectedWordBook; // 현재 선택된 단어장

        if (!selectedWordBook) {
            alert("삭제할 단어장을 선택하세요.");
            return;
        }

        // 삭제 확인 메시지
        const confirmMessage = selectedWordBook.title + "을(를) 삭제하시겠습니까? 단어장의 단어 데이터도 함께 삭제됩니다.";

        if (confirm(confirmMessage)) {
            // AJAX 요청
            $.ajax({
                url: "/compile/removeWordbook", // 서버의 삭제 처리 URL
                type: "POST",
                contentType: "application/json", // JSON 형태로 데이터 전송
                data: JSON.stringify({ids: [selectedWordBook.id]}),
                success: function(response) {
                    console.log(response);
                    if(response){
                        alert("단어장이 삭제되었습니다."); // 성공 메시지
                        // 화면에서 해당 단어장 요소 제거
                        $('[data-wordbook-id="' + selectedWordBook.id + '"]').remove();
                        $('#wordbookModal').modal('hide'); // 모달 닫기
                    } else {
                        alert("삭제 실패");
                    }
                },
                error: function(xhr, status, error) {
                    alert("단어장 삭제에 실패했습니다."); // 오류 메시지
                    console.error(error); // 오류 로그
                }
            });
        }
    }

    // 즐겨찾기 토글 처리
    handleFavoriteToggle() {
        if (this.selectedWordBook) {
            const action = this.selectedWordBook.bookmark ? '해제' : '등록';
            $.ajax({
                url: '/WordBook/bookmark',
                method: 'POST',
                data: {
                    wordBookId: this.selectedWordBook.id,
                    bookmark: !this.selectedWordBook.bookmark
                },
                success: (response) => {
                    if (response.success) {
                        this.selectedWordBook.bookmark = !this.selectedWordBook.bookmark;
                        alert(MESSAGES.FAVORITE_TOGGLE(this.selectedWordBook.title, action));
                        this.updateModalUI();
                        window.location.reload();
                    }
                },
                error: (xhr, status, error) => {
                    console.error('Error:', error);
                    alert('즐겨찾기 변경에 실패했습니다.');
                }
            });
        }
    }

    // 페이지 이동 처리
    handlePageNavigation(event) {
        if (!this.selectedWordBook) {
            console.log('No wordbook selected');
            return;
        }

        const buttonId = event.target.id;
        const pageType = this.getPageTypeFromButtonId(buttonId);
        if (!pageType) return;

        this.navigateToPage(pageType);
    }

    // 버튼 ID로부터 페이지 타입 결정
    getPageTypeFromButtonId(buttonId) {
        const pageTypes = {
            btnGoWriting: 'Writing',
            btnGoNews: 'News',
            btnGoTest: 'Test'
        };
        return pageTypes[buttonId];
    }

    // 페이지 이동 실행
    navigateToPage(pageType) {
        $.ajax({
            url: '/WordBook/getWords',
            type: 'GET',
            data: { wordBookId: this.selectedWordBook.id },
            success: (words) => {
                if (this.validateNavigation(pageType, words)) {
                    sessionStorage.setItem('selectedWords', JSON.stringify(words));
                    window.location.href = `/WordBook/${pageType}?wordBookId=${this.selectedWordBook.id}`;
                }
            },
            error: (xhr, status, error) => {
                console.error('Error:', error);
                alert('단어 데이터를 가져오는 중 오류가 발생했습니다.');
            }
        });
    }

    // 페이지 이동 유효성 검사
    validateNavigation(pageType, words) {
        if (pageType === 'Test') {
            if (words.length < 20) {
                alert('테스트를 진행하려면 단어장에 20개 이상의 단어가 필요합니다.');
                return false;
            }
            return confirm('테스트를 시작하시겠습니까?');
        }
        return true;
    }

    // 제목 수정 처리
    handleTitleEdit() {
        const currentTitle = this.selectedWordBook.title;
        const $title = $('#modalWordbookTitle');
        this.showTitleEditForm($title, currentTitle);
    }

    // 제목 수정 폼 표시
    showTitleEditForm($title, currentTitle) {
        var html = '<div class="input-group">' +
            '<input type="text" ' +
            'class="form-control" ' +
            'id="newTitle" ' +
            'value="' + currentTitle + '" ' +
            'maxlength="50" ' +
            'pattern="^[가-힣a-zA-Z0-9\\s,.()-_]+$" ' +
            'required>' +
            '<button class="btn btn-success btn-sm" id="btnSaveTitle">저장</button>' +
            '<button class="btn btn-secondary btn-sm" id="btnCancelEdit">취소</button>' +
            '</div>';

        $title.html(html);

        // input 이벤트 리스너 추가
        var $input = $('#newTitle');

        // 실시간 입력 검증
        $input.on('input', function(e) {
            var value = e.target.value;

            if (!value.trim()) {
                $input.addClass('is-invalid');
                return;
            }

            var validPattern = /^[가-힣a-zA-Z0-9\s,.()-_]+$/;
            if (!validPattern.test(value)) {
                $input.addClass('is-invalid');
                e.target.setCustomValidity('한글, 영문, 숫자와 일부 특수문자(,.()-_)만 사용할 수 있습니다.');
            } else {
                $input.removeClass('is-invalid');
                e.target.setCustomValidity('');
            }
        });

        // 키보드 이벤트
        $input.on('keypress', function(e) {
            var char = String.fromCharCode(e.keyCode);
            var validPattern = /[가-힣a-zA-Z0-9\s,.()-_]/;

            if (!validPattern.test(char)) {
                e.preventDefault();
            }
        });

        // paste 이벤트
        $input.on('paste', function(e) {
            e.preventDefault();
            var pastedText = (e.originalEvent.clipboardData || window.clipboardData).getData('text');
            var validPattern = /^[가-힣a-zA-Z0-9\s,.()-_]+$/;

            if (validPattern.test(pastedText)) {
                document.execCommand('insertText', false, pastedText);
            }
        });

        $('#btnSaveTitle').on('click', function() {
            if ($input[0].checkValidity()) {
                this.handleTitleSave(currentTitle);
            } else {
                $input.addClass('is-invalid');
            }
        }.bind(this));

        $('#btnCancelEdit').on('click', function() {
            this.updateModalUI();
        }.bind(this));
    }
    // 제목 저장 처리
    handleTitleSave(currentTitle) {
        const newTitle = $('#newTitle').val().trim();
        if (newTitle && newTitle !== currentTitle) {
            this.saveTitleToServer(newTitle)
                .then(() => {
                    // 성공적으로 저장되면 페이지 새로고침
                    window.location.reload();
                })
                .catch(error => {
                    console.error('제목 저장 실패:', error);
                    // 실패 시 원래 UI로 복구
                    this.updateModalUI();
                });
        } else {
            this.updateModalUI();
        }
    }

    // 제목 서버 저장
    saveTitleToServer(newTitle) {
        $.ajax({
            url: '/WordBook/updateTitle',
            method: 'POST',
            data: {
                wordBookId: this.selectedWordBook.id,
                newTitle: newTitle
            },
            success: (response) => {
                if (response.success) {
                    this.selectedWordBook.title = newTitle;
                    $('.wordbook-card[data-wordbook-id="' + this.selectedWordBook.id + '"] h5').text(newTitle);
                    this.updateModalUI();
                } else {
                    alert('제목 변경에 실패했습니다.');
                }
            },
            error: () => {
                alert('제목 변경 중 오류가 발생했습니다.');
            }
        });
    }

    // 정렬 처리
    handleSort(sortValue) {
        const $container = $('#wordbooksContainer');
        const $cards = $container.children('.wordbook-card').get();

        $cards.sort((a, b) => {
            const $a = $(a);
            const $b = $(b);

            switch(sortValue) {
                case 'createdAsc':
                    return $a.data('wordbook-id') - $b.data('wordbook-id');
                case 'createdDesc':
                    return $b.data('wordbook-id') - $a.data('wordbook-id');
                case 'titleAsc':
                    return $a.data('wordbook-title').localeCompare($b.data('wordbook-title'));
                default:
                    return 0;
            }
        });

        $container.append($cards);
    }

    // 즐겨찾기 필터 처리
    handleFavoriteFilter(showOnlyFavorites) {
        $('.wordbook-card').each(function() {
            const $card = $(this);
            const isBookmarked = $card.data('wordbook-bookmark');
            $card.toggle(!showOnlyFavorites || isBookmarked);
        });
    }

    // 이벤트 리스너 초기화
    initializeEventListeners() {
        $('#wordbooksContainer').on('click', '.wordbook-card', (e) => {
            const $card = $(e.currentTarget);
            // 텍스트로부터 직접 제목을 읽어오도록 수정
            const cardTitle = $card.find('h5').text();

            this.selectedWordBook = {
                id: $card.data('wordbook-id'),
                title: cardTitle,  // data 속성 대신 실제 텍스트 값 사용
                bookmark: $card.data('wordbook-bookmark')
            };
            console.log('Selected wordbook:', this.selectedWordBook);
            this.updateModalUI();
            $('#wordbookModal').modal('show');
        });

        $('#sortBy').on('change', (e) => this.handleSort($(e.target).val()));
        $('#showFavorites').on('change', (e) => this.handleFavoriteFilter($(e.target).is(':checked')));
        this.bindModalEvents();
    }
}

// 문서 로드 완료 시 WordbookManager 인스턴스 생성
$(document).ready(() => {
    new WordbookManager();
});