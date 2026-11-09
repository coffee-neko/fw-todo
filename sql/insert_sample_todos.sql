INSERT INTO todos (title, category, priority, due_date)
VALUES
    ('ポートフォリオのワイヤーを描く', 'デザイン', 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
    ('競合サイトを3つ調べる', 'マーケティング', 2, NULL),
    ('FW演習の課題を仕上げる', 'プログラミング', 1, DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
    ('ITパスポートの過去問を解く', '資格', 2, DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
    ('職務経歴書を書き直す', '就職活動', 3, DATE_ADD(CURDATE(), INTERVAL 60 DAY));