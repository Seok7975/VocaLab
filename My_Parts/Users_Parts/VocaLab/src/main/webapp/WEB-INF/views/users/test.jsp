<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>테스트페이지</title>
</head>
    <body>
<%--        <sec:authorize access="isAuthenticated()">--%>
<%--            <sec:authentication property="principal" var="user" />--%>
<%--            <pre>--%>
<%--                <!-- 디버깅용 출력 -->--%>
<%--                Principal Type: ${user.getClass().name}<br>--%>
<%--                Is OAuth2: ${user.attributes != null}<br>--%>
<%--                Properties: ${user}--%>
<%--            </pre>--%>
<%--            <a href="/logout">로그아웃</a>--%>
<%--        </sec:authorize>--%>



        <a href="/logout">로그아웃</a>

        <h2>회원 탈퇴</h2>

        <p>⚠️ 주의: 회원 탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.</p>

        <sec:authorize access="isAuthenticated()">
            <div class="form-group">
                <p>아이디: <strong><sec:authentication property="principal.userId"/></strong></p>
                <p>로그인 유형: <strong>${loginType}</strong></p>
            </div>

            <%-- 일반 로그인일 경우에만 비밀번호 확인 표시 --%>
            <c:if test="${loginType eq 'normal'}">
                <div class="confirmation-form">
                    <label for="password">비밀번호 확인:</label>
                    <input type="password" id="password" required>
                </div>
            </c:if>
        </sec:authorize>

        <button onclick="deleteAccount()">회원 탈퇴</button>

    <script>
        function deleteAccount() {
            if (!confirm('정말로 탈퇴하시겠습니까?')) {
                return;
            }

            const loginType = '${loginType}';

            if (loginType === 'normal') {
                // 일반 로그인의 경우 비밀번호 확인
                const password = document.getElementById('password').value;

                if (!password) {
                    alert('비밀번호를 입력해주세요.');
                    return;
                }

                // 비밀번호와 함께 DELETE 요청
                fetch('/delete', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        password: password
                    })
                })
                    .then(handleResponse)
                    .catch(handleError);
            } else {
                // 소셜 로그인의 경우 바로 DELETE 요청
                fetch('/delete', {
                    method: 'DELETE'
                })
                    .then(handleResponse)
                    .catch(handleError);
            }
        }

        function handleResponse(response) {
            if (!response.ok) {
                throw new Error('탈퇴 처리 중 오류가 발생했습니다.');
            }
            return response.text().then(message => {
                alert(message);
                window.location.href = '/login';
            });
        }

        function handleError(error) {
            alert(error.message);
        }
    </script>
    </body>
</html>