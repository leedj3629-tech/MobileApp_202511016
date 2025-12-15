미니 시간표 앱 (Jetpack Compose)

Jetpack Compose를 활용하여
요일별로 수업 시간을 확인할 수 있는 간단한 시간표 앱을 구현하였습니다.

1.개발 목적

Jetpack Compose 기반 UI 구성 방식 이해

상태(State)를 이용한 화면 변경 흐름 학습

수업에서 배운 기본 컴포넌트를 활용한 앱 제작

2.사용 기술

Language: Kotlin

IDE: Android Studio

UI: Jetpack Compose (Material3)

State 관리: remember, mutableStateOf

UI 구성: Scaffold, LazyColumn, Dialog

3.주요 기능

요일별 수업 목록 표시

수업 추가 / 삭제

시작 시간 기준 자동 정렬

간단한 입력값 검증

4.구현 특징

단일 Activity 구조로 Composable 함수 중심 설계

메모리 기반 상태 관리로 간단한 구조 유지

불필요한 기능을 제외하고 핵심 기능 위주로 구현

5.한계 및 보완점

앱 종료 시 데이터 초기화됨

추후 DataStore 또는 Room을 통한 영구 저장 기능 확장 가능