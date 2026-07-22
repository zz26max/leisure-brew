import { describe, expect, it } from 'vitest'
import { OrderStatus, orderStatusText } from '@leisure-brew/api-contract'

describe('order status copy', () => {
  it('covers every order state shown in the workbench', () => {
    expect(Object.keys(orderStatusText)).toHaveLength(Object.keys(OrderStatus).length)
    expect(orderStatusText[OrderStatus.PendingConfirmation]).toBe('待接单')
    expect(orderStatusText[OrderStatus.Delivering]).toBe('配送中')
  })
})
