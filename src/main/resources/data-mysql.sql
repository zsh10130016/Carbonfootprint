INSERT IGNORE INTO emission_factor (activity_type, sub_type, unit, factor_value, factor_name, description) VALUES
('TRANSPORT', 'BUS', 'km', 0.0800, '公交车出行', '适合城市通勤的低碳公共交通'),
('TRANSPORT', 'SUBWAY', 'km', 0.0500, '地铁出行', '城市轨道交通平均排放因子'),
('TRANSPORT', 'BIKE', 'km', 0.0050, '骑行', '近似视为极低碳出行'),
('TRANSPORT', 'WALK', 'km', 0.0000, '步行', '步行不产生直接化石能源排放'),
('TRANSPORT', 'TAXI', 'km', 0.1900, '出租车', '以城市出租燃油车平均排放估算'),
('TRANSPORT', 'TRAIN', 'km', 0.0400, '火车/高铁', '长距离公共交通较低排放'),
('HOME_ENERGY', 'ELECTRICITY', 'kWh', 0.5600, '家庭用电', '依据火电占比估算的平均排放因子'),
('HOME_ENERGY', 'NATURAL_GAS', 'm3', 2.1000, '天然气', '居民天然气直接燃烧排放'),
('HOME_ENERGY', 'WATER', 'ton', 0.3000, '生活用水', '供水与污水处理的综合排放估算'),
('FOOD', 'BEEF', 'kg', 27.0000, '牛肉消费', '高碳足迹肉类'),
('FOOD', 'PORK', 'kg', 12.1000, '猪肉消费', '中等偏高碳足迹肉类'),
('FOOD', 'CHICKEN', 'kg', 6.9000, '鸡肉消费', '相对较低碳足迹肉类'),
('FOOD', 'VEGETABLE', 'kg', 2.0000, '蔬菜消费', '植物性食物碳足迹较低'),
('FOOD', 'DAIRY', 'kg', 3.2000, '乳制品消费', '奶类加工综合排放估算');

INSERT IGNORE INTO advice_rule (activity_type, threshold_kg, period_days, title, description, suggestion) VALUES
('TRANSPORT', 25.00, 7, '交通排放偏高', '最近一周交通相关碳排放已经偏高，说明高频机动出行较多。', '尝试把短途打车替换为公交、地铁或骑行，每周至少安排 2 天绿色通勤。'),
('HOME_ENERGY', 35.00, 7, '家庭用能需要优化', '最近一周家庭用能排放偏高，可能存在空调或电器连续高负荷运行。', '优先检查待机耗电，空调温度建议控制在 26 摄氏度附近，并关注账单波动。'),
('FOOD', 20.00, 7, '饮食结构可继续改进', '最近一周饮食相关碳排放偏高，可能红肉占比偏高。', '尝试增加蔬菜、鸡肉等相对低碳食物比例，给自己安排几次轻负担饮食。');

INSERT IGNORE INTO article (title, summary, content, cover_image, author, published_at) VALUES
('日常通勤如何更低碳', '从地铁、公交、步行和骑行习惯入手，快速降低通勤排放。', '通勤是许多人最稳定的日常排放来源。对短距离场景，步行与骑行往往是最直接的减排方式；对中长距离场景，公交和地铁通常更具排放优势。可以先从每周固定两天绿色通勤做起，再逐渐扩大到更多工作日。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP),
('家庭节能的三个切入口', '空调、照明和待机电器，是最容易看到效果的家庭节能起点。', '家庭节能不一定意味着牺牲舒适度。可以先从空调温度、照明替换和电器待机管理开始。把能耗高的设备纳入固定检查清单，通常比一次性大改造更容易坚持。', 'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP),
('低碳饮食并不等于单调', '适当提高植物性食物占比，也能让饮食更丰富。', '饮食碳足迹与食材种类、加工链条和浪费情况都有关系。减少高碳肉类的频率，并增加应季蔬果、豆制品和低碳蛋白来源，既有利于控制排放，也更容易形成长期习惯。', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP);
