# 한 달 일정 보내기

Android `CalendarContract`에서 기기에 동기화된 월간 일정을 읽어 텍스트 파일 또는 메시지 본문으로 공유하는 읽기 전용 앱입니다.

## 기능

- `READ_CALENDAR` 런타임 권한과 거부·설정 이동 처리
- 기기 캘린더 목록과 표시 중인 캘린더 기본 선택
- 이전·다음 월 선택
- `CalendarContract.Instances` 기반 반복 일정 회차 전개
- 종일·여러 날·월 경계·0분 일정 처리
- 취소 일정 제외 및 `eventId:beginMillis` 인스턴스 식별
- 월간 목록 미리보기
- UTF-8 텍스트 파일 내보내기
- 메시지·카카오톡으로 보낼 수 있는 일반 텍스트 내보내기
- 읽기 전용 캐시 URI와 Android 공유 시트

앱은 `WRITE_CALENDAR`와 인터넷 권한을 선언하지 않습니다.

## 설치 파일 받기

GitHub의 최신 Release에서 `month-calendar-exporter-v0.1.2.apk`를 내려받아
설치합니다. 앱스토어를 거치지 않으므로 휴대폰에서 다운로드에 사용한 브라우저의
`출처를 알 수 없는 앱 설치`를 한 번 허용해야 할 수 있습니다.

자세한 순서는 [설치 안내](docs/INSTALL.md)를 참고하세요.

## 요구 환경

- Android Studio에 포함된 JDK 17
- Android SDK 36
- 최소 지원 Android 8.0(API 26)
- Gradle Wrapper 8.12

## 빌드와 테스트

Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

macOS 또는 Linux:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

디버그 APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Android Studio에서는 이 디렉터리를 프로젝트로 열고 `app` 구성을 실행합니다.

배포 APK는 프로젝트 밖에 안전하게 보관한 동일한 서명 열쇠를 사용해야 합니다.
서명 설정과 배포 절차는 [배포 안내](docs/DISTRIBUTION.md)를 참고하세요.

## 사용 순서

1. `일정 읽기 허용`을 누르고 휴대폰의 안내를 승인합니다.
2. `이전 달`·`다음 달` 버튼으로 가져올 달을 선택합니다.
3. `가져올 일정표`에서 하나 이상 선택합니다.
4. `이 달 일정 불러오기`를 누릅니다.
5. 목록을 확인한 뒤 `카톡·문자로 보내기` 또는 `파일로 보내기`를 누릅니다.
6. 화면 아래에 나타난 보낼 곳 목록에서 저장하거나 전달할 앱을 선택합니다.

## 데이터 구조

```text
Calendar Provider
  → CalendarProviderDataSource
  → CalendarRepository
  → CalendarEventNormalizer
  → MainActivity 미리보기
  → CalendarExportFormatter
  → 텍스트 파일 또는 일반 텍스트
  → Android Sharesheet
```

종일 일정의 종료 날짜는 제외 경계입니다. 예를 들어 `2026-08-24` 하루짜리 종일 일정은 시작일 `2026-08-24`, 종료일 `2026-08-25`로 내보냅니다.

## 현재 범위

- 개인 프로필의 표준 Android Calendar Provider만 조회합니다.
- 업무 프로필의 Enterprise Calendar URI는 아직 지원하지 않습니다.
- 참석자와 알림은 추출하지 않습니다.
- 서버 업로드나 백그라운드 자동 추출은 없습니다.
- 서버에만 있고 기기에 동기화되지 않은 일정은 조회되지 않습니다.
- 자체 비공개 DB만 사용하는 타사 캘린더 일정은 조회되지 않을 수 있습니다.

테스트 결과는 [docs/TESTING.md](docs/TESTING.md), 개인정보 안내는
[docs/PRIVACY.md](docs/PRIVACY.md)를 참고하세요.
