# DB設計書

## 1. この設計書の目的と対象

この設計書は、個人用ToDoアプリがMySQL（データを保存する仕組み）に保存するデータを決めるものです。

- 使用するテーブル（同じ種類のデータを行と列で保存する箱）は、`todos` の1つだけです。
- やること1件を、`todos` の1行として保存します。
- ログイン利用者や共有利用者を管理するテーブルは作りません。利用者は1人という要件のためです。
- 登録・編集・削除の記録は、要件に従ってアプリケーションのログ（動作の記録）に出します。テーブルは `todos` だけという前提のため、ログ専用テーブルは作りません。ログには操作の種類と `id` だけを残し、`title` と `detail` は残しません。

## 2. テーブルの概要

| 項目 | 内容 |
|---|---|
| テーブル名 | `todos` |
| 用途 | やること、メモ、ジャンル、優先度、期限、完了状態、登録日時、更新日時を保存する |
| 1行の意味 | 1件のやること |
| 主キー | `id`（各行を重複なく見分ける番号） |
| DBMS | MySQL（データベース管理システム：データを保存・検索するソフトウェア） |

## 3. カラム定義

カラムは、テーブル内の項目（列）のことです。`NOT NULL` は「値を空にできない」、`NULL` は「値を空にできる」という意味です。

| カラム名 | 論理名 | 型 | NULL | 既定値 | 制約・設定 | 要件との対応 |
|---|---|---|---|---|---|---|
| `id` | 識別番号 | `BIGINT` | 不可 | なし | 主キー。`AUTO_INCREMENT`（登録ごとに番号を自動で1ずつ増やす） | 1件を見分ける番号。編集・削除URLの `{id}` に使用。入力画面には出さず自動設定。 |
| `title` | やること | `VARCHAR(255)` | 不可 | なし | 最大255文字 | 登録・編集で必須。名前による部分一致検索と一覧表示に使用。空白だけ、256文字以上はアプリ側で保存前に確認。 |
| `detail` | メモ | `VARCHAR(255)` | 可 | `NULL` | 最大255文字 | 登録・編集で任意。256文字以上はアプリ側で保存前に確認。 |
| `category` | ジャンル | `VARCHAR(255)` | 不可 | なし | 5候補だけを許可する `CHECK`（保存値を制限する規則） | 登録・編集で必須。ジャンルによる完全一致検索と一覧表示に使用。 |
| `priority` | 優先度 | `INT` | 不可 | `2` | `1`（高）・`2`（中）・`3`（低）だけを許可する `CHECK` | 登録・編集で必須。既定値は中（`2`）。一覧では高・中・低に表示。 |
| `due_date` | 期限 | `DATE` | 可 | `NULL` | 日付のみを保存 | 登録・編集で任意。一覧の表示と、期限が近い順／遠い順の並び替えに使用。 |
| `completed` | 完了状態 | `BOOLEAN` | 不可 | `FALSE` | `BOOLEAN` はMySQLでは `TINYINT(1)` として扱われる。`FALSE`（未完了、`0`）／`TRUE`（完了、`1`） | 編集時の完了チェック欄と一覧表示に使用。既定値は未完了。 |
| `created_at` | 登録日時 | `DATETIME` | 不可 | 登録時刻 | `DEFAULT CURRENT_TIMESTAMP`（登録時にMySQLが自動設定） | 入力画面には出さない。登録日時を自動保存。 |
| `updated_at` | 更新日時 | `DATETIME` | 不可 | 登録時刻 | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`（登録時と更新時にMySQLが自動設定） | 入力画面には出さない。最後に編集した日時を自動保存。 |

### 3.1 ジャンルの保存値

`category` に保存できる値は、次の5つだけです。画面では自由入力ではなく選択欄にします。

| 保存する値 |
|---|
| `デザイン` |
| `マーケティング` |
| `プログラミング` |
| `資格` |
| `就職活動` |

### 3.2 優先度・完了状態の値

| カラム | 保存する値 | 画面に表示する言葉 |
|---|---|---|
| `priority` | `1` | 高 |
| `priority` | `2` | 中 |
| `priority` | `3` | 低 |
| `completed` | `1`（`TRUE`） | 完了 |
| `completed` | `0`（`FALSE`） | 未完了 |

## 4. インデックス設計

インデックスは、指定した条件でデータを探したり並べたりしやすくする目次のような仕組みです。

| 名前 | 対象カラム | 目的 |
|---|---|---|
| `PRIMARY` | `id` | 1件を番号で特定し、編集・削除対象を素早く探す。 |
| `idx_todos_category_due_date` | `category`, `due_date` | ジャンルで絞り込み、期限順に一覧表示する処理を助ける。 |
| `idx_todos_due_date` | `due_date` | 全件を期限が近い順／遠い順に並べる処理を助ける。 |

`title` の検索は「入力語を途中に含むか」を調べる部分一致検索です。`%入力語%` の形になるため、通常のインデックスでは効果が限定的です。今回の要件では、専用の検索用インデックスは作りません。

## 5. 要件とカラムの対応

| 要件 | 対応するカラム | 設計での扱い |
|---|---|---|
| やることを1件ずつ登録・編集・削除する | `id`, `title`, `detail`, `category`, `priority`, `due_date`, `completed` | `id` で対象の1件を特定し、他の項目を保存・更新する。削除時は該当行を削除する。 |
| やることを必須入力にする | `title` | `NOT NULL`。空文字・半角／全角空白だけの入力はアプリ側で保存前に確認する。 |
| メモを任意にする | `detail` | `NULL` を許可する。 |
| やること・メモを255文字以内にする | `title`, `detail` | `VARCHAR(255)`。入力時の文字数確認もアプリ側で行う。 |
| ジャンルを5候補から必須選択にする | `category` | `NOT NULL` と `CHECK` で5つの値に限定する。 |
| 優先度を高・中・低から必須選択にする | `priority` | `NOT NULL` と `CHECK` で `1`〜`3` に限定する。既定値は `2`（中）。 |
| 期限を任意にする | `due_date` | `NULL` を許可する。 |
| 完了／未完了を保存・表示する | `completed` | `BOOLEAN` で保存し、既定値は `FALSE`（未完了）。 |
| 名前で絞り込む | `title` | `title` に入力語を含む行を検索する。 |
| ジャンルで絞り込む | `category` | 選択したジャンルと完全一致する行を検索する。 |
| 期限順に並べ替える | `due_date` | 昇順（近い順）または降順（遠い順）で並べる。 |
| 編集・削除の対象を指定する | `id` | URLの `{id}` と照合して対象の1行を取得する。 |
| 登録日時・更新日時を自動設定する | `created_at`, `updated_at` | MySQLの自動設定を使用する。 |
| 操作の記録に対象番号を残す | `id` | アプリケーションログに操作種別と `id` を出力する。`title`・`detail` は出力しない。 |

## 6. CREATE TABLE 文（DDL）

DDL（データベース定義言語：テーブルを作るためのSQL文）は次のとおりです。MySQL 8.0.16以降を前提とし、`CHECK` 制約が有効になる環境で実行します。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todos_completed
        CHECK (completed IN (0, 1)),
    INDEX idx_todos_category_due_date (category, due_date),
    INDEX idx_todos_due_date (due_date)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_ja_0900_as_cs;
```

`utf8mb4` は、日本語を含む文字を保存できる文字コードです。`COLLATE` は文字の比較方法です。`utf8mb4_ja_0900_as_cs` は、大文字・小文字やひらがな・カタカナの違いを区別する比較方法です。利用するMySQLにこの照合順序がない場合は、利用可能な `utf8mb4` の照合順序に置き換えます。

## 7. 保存時の補足ルール

- `id`、`created_at`、`updated_at` は画面の入力欄に出さず、MySQLに自動設定させます。
- `title` の空文字や空白だけの判定、`title` と `detail` の255文字以内の判定は、利用者にその場でメッセージを出せるよう、アプリケーション側でも必ず行います。
- `due_date` が未入力の場合は `NULL` を保存します。期限順の一覧で期限未設定の行をどの位置に置くかは、画面設計・実装時に表示ルールとして決めます。
- `updated_at` は編集で行を更新したときに自動で変わります。完了状態の変更も編集なので更新日時が変わります。
