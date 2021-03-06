<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
	String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <!-- <meta name="viewport" content="width=device-width, initial-scale=1.0" /> -->
    <title>COFFEE</title>
    <link rel="stylesheet" href="<%=cp%>/resource/css/reset.css" />
    <link rel="stylesheet" href="<%=cp%>/resource/css/layout.css" />
    <link rel="stylesheet" href="<%=cp%>/resource/css/created.css" />
<script type="text/javascript">
function sendMenu() {
    var f = document.menuForm;

	var str = f.menuName.value;
    if(!str) {
        alert("메뉴 이름을 입력하세요. ");
        f.subject.focus();
        return;
    }

	str = f.text.value;
    if(!str) {
        alert("설명을 입력하세요. ");
        f.content.focus();
        return;
    }

    var mode="${mode}";
	  if(mode=="created"||mode=="update" && f.thumbnail.value!="") {
		if(! /(\.gif|\.jpg|\.png|\.jpeg)$/i.test(f.thumbnail.value)) {
			alert('이미지 파일만 가능합니다. !!!');
			f.thumbnail.focus();
			return;
		}
	  }
	  
  	  if(mode=="created")
    		f.action="<%=cp%>/menu/createdMenu_ok.do";
  	  else if(mode=="update")
    		f.action="<%=cp%>/menu/update_ok.do";

    f.submit();
}

</script>
  </head>
  <body>
    <div id="wrap">
      <header id="header">
        <jsp:include page="/WEB-INF/views/layout/header.jsp"/>
      </header>
      <br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
      <div class="menuCreateForm">
      <h3> | 메뉴 등록</h3>
      <hr>
		<div>
		<form class="menuForm1" style="width: 100%" name="menuForm" method="post" enctype="multipart/form-data">
			<table class="createtable">
				<tr>
					<td class="menulist"> 메뉴 카테고리 번호 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td> 
						<select name="categoryNum" style="width: 120px; height:30px;">
							<option value="none"> 선 택 </option>
							<option value="1" selected="selected"> 커피 </option>
							<option value="2"> 에이드 </option>
							<option value="3"> 베이커리 </option>
						</select> 
					</td>
					<td><br></td>
				</tr>
				<tr>
					<td class="menulist"> 메뉴 이름 </td>
					<td> <input class="menuinput" type="text" name="menuName" value="${dto.menuName}"> </td>
				</tr>
				<tr>
					<td class="menulist"> 이미지 파일 </td>
					<td> <input class="menuinput" type="file" name="thumbnail" accept="image/*"></td>
				</tr>
				<tr>
					<td class="menulist"> 메뉴 소개글 </td>
					<td> <input class="menuinput" type="text" name="text" value="${dto.text}"> </td>
				</tr>
				<tr style="border-bottom: 0px solid white;">
					<td class="menulist"> 가격 </td>
					<td> <input class="menuinput" type="text" name="price" value="${dto.price}"> </td>
				</tr>
				<tr class="menubuttontr">
				</tr>
			</table>
			<div>
						<c:if test="${mode=='update'}">
							<input type="hidden" name="menuNum" value="${dto.menuNum}"/>
							<input type="hidden" name="saveFilename" value="${dto.thumbnail}"/>
						</c:if>
						<div align="center" style="padding-top: 10px;">
							<button class="menubutton" type="button" onclick="sendMenu();">${mode=='update'?'수정완료':'등록하기'}</button>
							<button class="menubutton" type="reset" class="btn">다시입력</button>
			        		<button class="menubutton" type="button" class="btn" onclick="javascript:location.href='<%=cp%>/menu/coffee.do';">${mode=='update'?'수정취소':'등록취소'}</button>
						</div>
				</div>
			</form>
		</div>
		<hr>
		</div>
		<br><br><br><br><br><br><br><br><br>
      <footer id="footer">
        <jsp:include page="/WEB-INF/views/layout/footer.jsp"/>
      </footer>
    </div>
  </body>
</html>
