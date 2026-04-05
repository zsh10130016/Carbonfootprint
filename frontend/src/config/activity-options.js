export const activityOptions = {
  TRANSPORT: {
    label: '绿色出行',
    unit: 'km',
    subTypes: [
      { value: 'BUS', label: '公交' },
      { value: 'SUBWAY', label: '地铁' },
      { value: 'BIKE', label: '骑行' },
      { value: 'WALK', label: '步行' },
      { value: 'TAXI', label: '出租车' },
      { value: 'TRAIN', label: '火车/高铁' }
    ]
  },
  HOME_ENERGY: {
    label: '家庭用能',
    unit: 'kWh',
    subTypes: [
      { value: 'ELECTRICITY', label: '用电(kWh)', unit: 'kWh' },
      { value: 'NATURAL_GAS', label: '天然气(m3)', unit: 'm3' },
      { value: 'WATER', label: '用水(ton)', unit: 'ton' }
    ]
  },
  FOOD: {
    label: '饮食消费',
    unit: 'kg',
    subTypes: [
      { value: 'BEEF', label: '牛肉' },
      { value: 'PORK', label: '猪肉' },
      { value: 'CHICKEN', label: '鸡肉' },
      { value: 'VEGETABLE', label: '蔬菜' },
      { value: 'DAIRY', label: '乳制品' }
    ]
  }
}
