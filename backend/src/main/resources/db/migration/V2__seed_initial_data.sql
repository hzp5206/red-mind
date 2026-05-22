INSERT INTO templates (category, title, content_example, tags, style, is_active)
SELECT 'beauty', '熬夜党提亮精华种草模板', '最近皮肤又黄又暗？我用这套思路来写更容易出效果...', '护肤,提亮,精华', 'good_item', 1
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE title = '熬夜党提亮精华种草模板');

INSERT INTO templates (category, title, content_example, tags, style, is_active)
SELECT 'food', '打工人低卡早餐分享模板', '想要早上省时又不无聊，可以从真实通勤场景切入...', '早餐,低卡,打工人', 'vlog', 1
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE title = '打工人低卡早餐分享模板');

INSERT INTO templates (category, title, content_example, tags, style, is_active)
SELECT 'fashion', '初夏通勤穿搭合集模板', '把“显高、好搭、不会出错”拆成三段来讲，转化更自然...', '穿搭,通勤,显高', 'collection', 1
WHERE NOT EXISTS (SELECT 1 FROM templates WHERE title = '初夏通勤穿搭合集模板');

INSERT INTO sensitive_words (word, replacement, is_active)
SELECT '最有效', '超有效', 1
WHERE NOT EXISTS (SELECT 1 FROM sensitive_words WHERE word = '最有效');

INSERT INTO sensitive_words (word, replacement, is_active)
SELECT '国家级', '高口碑', 1
WHERE NOT EXISTS (SELECT 1 FROM sensitive_words WHERE word = '国家级');

INSERT INTO sensitive_words (word, replacement, is_active)
SELECT '绝对', '更稳妥', 1
WHERE NOT EXISTS (SELECT 1 FROM sensitive_words WHERE word = '绝对');

INSERT INTO sensitive_words (word, replacement, is_active)
SELECT '第一', '热门', 1
WHERE NOT EXISTS (SELECT 1 FROM sensitive_words WHERE word = '第一');
