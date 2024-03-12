<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class='fs_install_manual_root full'>
    <table class='full'>
        <colgroup>
            <col style='width: 120px;'/>
            <col style='width: 160px;'/>
            <col />
        </colgroup>
        <tbody>
            <tr>
                <th class='lang_element' data-lang-en="Title">타이틀</th>
                <td class='lang_element' data-lang-en="Main menu title" colspan='2'>메인 화면 타이틀</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Root Directory">최상위 경로</th>
                <td class='lang_element' data-lang-en="The root directory to share." colspan='2'>공유할 파일이 있는 디렉토리</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Password">암호</th>
                <td class='lang_element' data-lang-en="The password for installation only which is in fs.properties." colspan='2'>설치용 비밀번호 (fs.properties 에 기재됨)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Download Max Size">다운로드 MAX</th>
                <td class='lang_element' data-lang-en="Max sizes of file to share." colspan='2'>공유할 파일의 최대 크기</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Previewing Max Size">미리보기 MAX</th>
                <td class='lang_element' data-lang-en="Max sizes of file to allow previewing." colspan='2'>미리보기 허용할 파일의 최대 크기</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Output Count">출력 최대 갯수</th>
                <td class='lang_element' data-lang-en="File's count on single network request." colspan='2'>네트워크 요청 한 번에 담을 최대 파일 갯수</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Captcha">캡차</th>
                <td class='lang_element' data-lang-en="Prevent bots and macros." colspan='2'>봇, 매크로 이용 방지</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Account" rowspan='3'>계정</th>
                <td class='lang_element' data-lang-en="Use 'user account' feature." colspan='2'>사용자 계정 기능 사용</td>
            </tr>
            <tr>
                <td class='lang_element' data-lang-en="Login Fails Limit" style='font-weight: bolder'>로그인 실패 횟수 제한</td>
                <td class='lang_element' data-lang-en="If the user failed to login much time, then user's account will be locked some minutes.">로그인 실패 횟수가 이 이상 누적되면 몇 분간 계정이 잠김</td>
            </tr>
            <tr>
                <td class='lang_element' data-lang-en="Token Lifetime" style='font-weight: bolder'>토큰 유효시간</td>
                <td class='lang_element' data-lang-en="Use 'token' feature when this value is 1 or bigger. Use 'token' to keep login status without JSP session.">1 이상 설정 시 토큰 (JSP 세션 없이도 로그인 유지) 기능 사용, 그 토큰의 유효 시간 </td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Admin ID">관리자 ID</th>
                <td class='lang_element' data-lang-en="The root account ID. (Will be created after installation)" colspan='2'>최상위 권한을 가질 사용자의 ID (설치 후 이 사용자가 생성됨)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Admin Password">관리자 계정 암호</th>
                <td class='lang_element' data-lang-en="The root account' password." colspan='2'>최상위 권한을 가질 사용자의 암호</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Admin Nickname">관리자 별명</th>
                <td class='lang_element' data-lang-en="The root account' nickname." colspan='2'>최상위 권한을 가질 사용자의 별명 (로그인 후 ID 대신 화면에 출력)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Salt">Salt</th>
                <td class='lang_element' data-lang-en="The salt value to affect security encryption. Input 2~5 alphabet letters." colspan='2'>이 값은 암호화/해시 작업에 영향을 끼침. 2~5자의 알파벳 입력.</td>
            </tr>
            <tr>
                <th rowspan='5'>기타</th>
                <td colspan='2'></td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Read-Only mode">읽기 전용 모드</th>
                <td class='lang_element' data-lang-en="Install FS as read-only mode.">FS를 읽기 전용 모드로 설치 (프로그램을 통한 파일 업로드, 삭제, 설정 변경이 차단됨)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Read file's icon">파일 아이콘 읽기</th>
                <td class='lang_element' data-lang-en="Show file's icon on list.">파일 목록에 파일의 아이콘도 출력 (부하가 커질 수 있음)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Use Console">콘솔 사용</th>
                <td class='lang_element' data-lang-en="Use text based console interface.">텍스트 기반 콘솔 인터페이스 사용 (우측 상단에 '콘솔' 버튼이 나타남)</td>
            </tr>
            <tr>
                <th class='lang_element' data-lang-en="Use Session">세션 사용</th>
                <td class='lang_element' data-lang-en="Use JSP based session feature. (If you use 'user account' or 'captcha' feature, you should turn on 'token' or 'session' feature.)">JSP 기반 세션 접근 (토큰 또는 세션이 있어야 사용자 로그인 및 캡차 사용이 가능함)</td>
            </tr>
        </tbody>
    </table>
</div>