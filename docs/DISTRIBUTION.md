# 배포 안내

## 배포 방식

- 소스 코드는 Git 저장소에서 관리한다.
- APK와 확인값 파일은 Git 커밋에 넣지 않고 GitHub Release에만 첨부한다.
- 버전 태그는 `v0.1.0` 형식을 사용한다.
- APK 이름은 `month-calendar-exporter-v<버전>.apk` 형식을 사용한다.

## 서명 파일

로컬 빌드는 Git에서 제외된 다음 파일을 사용한다.

```text
.secrets/calendar-exporter-release.jks
keystore.properties
```

두 파일은 GitHub에 올리지 않는다. 암호화된 저장소와 별도 장치에 백업하고, 앱의
전체 수명 동안 같은 열쇠를 사용한다. 열쇠를 잃으면 기존 설치본을 업데이트할 수 없다.

## 배포 전 점검

1. `versionCode`를 이전보다 크게 올린다.
2. 사용자에게 보이는 `versionName`을 정한다.
3. `test`, `lintRelease`, `assembleRelease`를 실행한다.
4. APK 서명과 SHA-256을 확인한다.
5. 실제 기기에서 새 설치와 기존 버전 위 덮어쓰기를 확인한다.
6. Release 설명, APK, SHA-256 파일을 게시한다.

Windows PowerShell 빌드 명령:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test lintRelease assembleRelease
```

배포 파일은 `app/build/outputs/apk/release/app-release.apk`에서 생성된다.
