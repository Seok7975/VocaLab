package com.dev.vocalab.board;

public class PagingUtil {

	public static String pagingImg(
			int totalRecordCount,
			int pageSize,
			int blockPage,
			int pageNum,
			String page,
			String searchWord
	) {
		StringBuilder pagingStr = new StringBuilder();

		// 전체 페이지 수 계산
		int totalPage = (int) (Math.ceil(((double) totalRecordCount / pageSize)));

		// 현재 페이지 블록 계산
		int intTemp = (((pageNum - 1) / blockPage) * blockPage) + 1;

		// 검색어가 있는 경우, URL에 추가
		String searchParam = (searchWord != null && !searchWord.isEmpty())
				? "&searchWord=" + searchWord
				: "";

		// 첫 페이지 및 이전 페이지 블록 링크
		if (intTemp != 1) {
			pagingStr.append("<a href='").append(page).append("pageNum=1").append(searchParam)
					.append("'><img src='/images/board/paging1.gif'></a>");
			pagingStr.append("&nbsp;");
			pagingStr.append("<a href='").append(page).append("pageNum=")
					.append(intTemp - blockPage).append(searchParam)
					.append("'><img src='/images/board/paging2.gif'></a>");
		}

		// 현재 블록 내 페이지 번호 링크
		int blockCount = 1;
		while (blockCount <= blockPage && intTemp <= totalPage) {
			if (intTemp == pageNum) {
				pagingStr.append("&nbsp;").append(intTemp).append("&nbsp;");
			} else {
				pagingStr.append("&nbsp;<a href='").append(page).append("pageNum=")
						.append(intTemp).append(searchParam).append("'>")
						.append(intTemp).append("</a>&nbsp;");
			}
			intTemp++;
			blockCount++;
		}

		// 다음 페이지 블록 및 마지막 페이지 링크
		if (intTemp <= totalPage) {
			pagingStr.append("<a href='").append(page).append("pageNum=")
					.append(intTemp).append(searchParam)
					.append("'><img src='/images/board/paging3.gif'></a>");
			pagingStr.append("&nbsp;");
			pagingStr.append("<a href='").append(page).append("pageNum=")
					.append(totalPage).append(searchParam)
					.append("'><img src='/images/board/paging4.gif'></a>");
		}

		return pagingStr.toString();
	}
}
