<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>/board/modify.jsp</h1>
	<h2>글 수정하기</h2>
	
	<div class="box-header with-border">
		<h3 class="box-title">게시판 글 수정하기</h3>
	</div>
	<form role="form" method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<input type="hidden" name="board_no" value="${boardVO.board_no }" />
		<input type="hidden" name="keyword" value="${cri.keyword }">
		<div class="box-body">
			<div class="form-group">
				<label for="exampleInputEmail1">제 목</label> <input type="text" class="form-control" id="exampleInputEmail1" name="title" value="${boardVO.title }">
			</div>

			<div class="form-group">
				<label>이 름</label> <input type="text" class="form-control" name="employee_id" value="${boardVO.employee_id }" />
			</div>

			<div class="form-group">
				<label>내 용</label>
				<textarea class="form-control" rows="3" name="content">${boardVO.content }</textarea>
			</div>

			<div class="box-footer">
				<button type="submit" class="btn btn-danger">글 수정</button>
			</div>
		</div>
	</form>
	
	<a href="/board/list">목록으로</a>
</body>
</html>