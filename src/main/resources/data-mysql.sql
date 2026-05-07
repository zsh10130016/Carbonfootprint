-- 采用“活动数据 × 排放因子”口径；重复执行时会刷新已有因子。
INSERT INTO emission_factor (activity_type, sub_type, unit, factor_value, factor_name, description) VALUES
('TRANSPORT', 'BUS', 'km', 0.0270, '公交车出行', 'IPCC 柴油燃烧因子结合城市公交平均载客量折算为人公里'),
('TRANSPORT', 'SUBWAY', 'km', 0.0300, '地铁出行', '按电力消费因子与轨道交通单位人公里电耗简化估算'),
('TRANSPORT', 'BIKE', 'km', 0.0000, '骑行', '按 IPCC 直接排放口径，骑行直接化石能源排放为 0'),
('TRANSPORT', 'WALK', 'km', 0.0000, '步行', '按 IPCC 直接排放口径，步行直接化石能源排放为 0'),
('TRANSPORT', 'TAXI', 'km', 0.1850, '出租车', 'IPCC 汽油燃烧因子结合乘用车典型油耗折算为车公里'),
('TRANSPORT', 'TRAIN', 'km', 0.0250, '火车/高铁', '按电力消费因子与铁路单位人公里电耗简化估算'),
('HOME_ENERGY', 'ELECTRICITY', 'kWh', 0.5777, '家庭用电', '采用生态环境部、国家统计局和国家能源局发布的 2024 年全国电力平均碳足迹因子'),
('HOME_ENERGY', 'NATURAL_GAS', 'm3', 2.1840, '天然气', '按 IPCC 天然气默认 CO2 因子 56100 kg/TJ 及常用低位热值折算'),
('HOME_ENERGY', 'WATER', 'ton', 0.3000, '生活用水', '按供水和污水处理电耗乘以电力因子的简化估算'),
('FOOD', 'BEEF', 'kg', 60.0000, '牛肉消费', 'Poore & Nemecek/OWID 食物 LCA 全球平均 CO2e 因子'),
('FOOD', 'PORK', 'kg', 7.6000, '猪肉消费', 'Poore & Nemecek/OWID 食物 LCA 全球平均 CO2e 因子'),
('FOOD', 'CHICKEN', 'kg', 6.9000, '鸡肉消费', 'Poore & Nemecek/OWID 食物 LCA 全球平均 CO2e 因子'),
('FOOD', 'VEGETABLE', 'kg', 0.5000, '蔬菜消费', 'Poore & Nemecek/OWID 植物性食物 LCA 简化因子'),
('FOOD', 'DAIRY', 'kg', 3.2000, '乳制品消费', 'Poore & Nemecek/OWID 乳制品 LCA 简化因子'),
('FOOD', 'RICE', 'kg', 4.0000, '大米消费', 'Poore & Nemecek/OWID 稻米 LCA 全球平均 CO2e 因子'),
('FOOD', 'EGG', 'kg', 4.5000, '鸡蛋消费', 'Poore & Nemecek/OWID 鸡蛋 LCA 全球平均 CO2e 因子'),
('FOOD', 'TOFU', 'kg', 3.0000, '豆腐消费', 'Poore & Nemecek/OWID 豆制品 LCA 简化因子')
ON DUPLICATE KEY UPDATE
    factor_value = VALUES(factor_value),
    factor_name = VALUES(factor_name),
    description = VALUES(description);

INSERT IGNORE INTO advice_rule (activity_type, threshold_kg, period_days, title, description, suggestion) VALUES
('TRANSPORT', 25.00, 7, '交通排放偏高', '最近一周交通相关碳排放已经偏高，说明高频机动出行较多。', '尝试把短途打车替换为公交、地铁或骑行，每周至少安排 2 天绿色通勤。'),
('HOME_ENERGY', 35.00, 7, '家庭用能需要优化', '最近一周家庭用能排放偏高，可能存在空调或电器连续高负荷运行。', '优先检查待机耗电，空调温度建议控制在 26 摄氏度附近，并关注账单波动。'),
('FOOD', 20.00, 7, '饮食结构可继续改进', '最近一周饮食相关碳排放偏高，可能红肉占比偏高。', '尝试增加蔬菜、鸡肉等相对低碳食物比例，给自己安排几次轻负担饮食。');

INSERT IGNORE INTO article (title, summary, content, cover_image, author, published_at) VALUES
('日常通勤如何更低碳', '从地铁、公交、步行和骑行习惯入手，快速降低通勤排放。', '通勤是许多人最稳定的日常排放来源。对短距离场景，步行与骑行往往是最直接的减排方式；对中长距离场景，公交和地铁通常更具排放优势。可以先从每周固定两天绿色通勤做起，再逐渐扩大到更多工作日。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP),
('家庭节能的三个切入口', '空调、照明和待机电器，是最容易看到效果的家庭节能起点。', '家庭节能不一定意味着牺牲舒适度。可以先从空调温度、照明替换和电器待机管理开始。把能耗高的设备纳入固定检查清单，通常比一次性大改造更容易坚持。', 'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP),
('低碳饮食并不等于单调', '适当提高植物性食物占比，也能让饮食更丰富。', '饮食碳足迹与食材种类、加工链条和浪费情况都有关系。减少高碳肉类的频率，并增加应季蔬果、豆制品和低碳蛋白来源，既有利于控制排放，也更容易形成长期习惯。', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=80', '低碳实验室', CURRENT_TIMESTAMP);
