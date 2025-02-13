<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입</title>
    <!--    <link rel="stylesheet" type="text/css" href="/css/login/styles2.css">-->
    <!--    <link href="/main.css" rel="stylesheet">-->
</head>
<body>
<h1>Sign in</h1>
<form action="/register" method="post">
    <!-- userName(Id) 입력란 -->
    <div class="userId">
        <label for="userId">ID</label>
        <input type="text" id="userId" name="userId" placeholder="Enter Your ID" autocomplete="off" required>
    </div>
    <!-- Password 입력란 -->
    <div class="userPassword">
        <label for="userPassword">Password</label>
        <input type="password" id="userPassword" name="userPassword" placeholder="Enter Your Password" autocomplete="off" required>
    </div>
    <!-- userName(이름) 입력란 -->
    <div class="userName">
        <label for="userName">Name</label>
        <input type="text" id="userName" name="userName" placeholder="Enter Your Name" autocomplete="off" required>
    </div>
    <!-- userNickname(닉네임) 입력란 -->
    <div class="userNickname">
        <label for="userNickname">NickName</label>
        <input type="text" id="userNickname" name="userNickname" placeholder="Enter Your NickName" autocomplete="off" required>
    </div>
    <!-- userEmail(이메일) 입력란 유효성검사 필요-->
    <div class="userEmail">
        <label for="userEmail">Email</label>
        <input type="text" id="userEmail" name="userEmail" placeholder="Enter Your userEmail" autocomplete="off" required>
    </div>
    <!-- birthDate(생년월일) 입력란 -->
<%--    <div class="birthDateAndGender">--%>
<%--        <label for="birthDate">BirthDateAndSex</label>--%>
<%--        <input type="text" id="birthDate" name="birthDate" placeholder="Enter Your birthDate" autocomplete="off" required>--%>
<%--        <input type="number" id="gender" name="gender" placeholder="Enter the first digit of the last 7 digits of your Resident Registration Number." autocomplete="off" required>--%>
<%--    </div>--%>
    <div>
        <label>Birth Date and Gender</label>
        <div>
            <!-- 년도 입력 -->
            <input type="text" id="year" name="year" list="yearList" placeholder="YYYY" maxlength="4" pattern="[0-9]{4}">
            <datalist id="yearList">
                <script>
                    const currentYear = new Date().getFullYear();
                    for(let i = currentYear; i >= 1900; i--) {
                        document.write(`<option value="${i}">`);
                    }
                </script>
            </datalist>

            <!-- 월 입력 -->
            <input type="text" id="month" name="month" list="monthList" placeholder="MM" maxlength="2" pattern="[0-9]{1,2}">
            <datalist id="monthList">
                <script>
                    for(let i = 1; i <= 12; i++) {
                        const month = i.toString().padStart(2, '0');
                        document.write(`<option value="${month}">`);
                    }
                </script>
            </datalist>

            <!-- 일 입력 -->
            <input type="text" id="day" name="day" list="dayList" placeholder="DD" maxlength="2" pattern="[0-9]{1,2}">
            <datalist id="dayList">
                <script>
                    for(let i = 1; i <= 31; i++) {
                        const day = i.toString().padStart(2, '0');
                        document.write(`<option value="${day}">`);
                    }
                </script>
            </datalist>
        </div>

        <!-- 성별 선택 -->
        <div>
            <label>Gender</label>
            <label>
                <input type="radio" name="gender" value="1" required>
                Male
            </label>
            <label>
                <input type="radio" name="gender" value="2" required>
                Female
            </label>
        </div>
    </div>

    <!-- Submit 버튼 -->
    <button type="submit" class="submit-btn">Sign up</button>
</form>
</body>
</html>