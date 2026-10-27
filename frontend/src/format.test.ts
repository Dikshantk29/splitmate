import {describe,it,expect} from 'vitest';import{money}from'./format';
describe('money',()=>{it('formats currency consistently',()=>{expect(money(1234.5)).toBe('$1,234.50');});it('renders zero balances',()=>{expect(money(0)).toBe('$0.00');});});
