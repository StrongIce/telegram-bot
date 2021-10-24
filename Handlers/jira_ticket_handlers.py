from aiogram import Dispatcher, types
from aiogram.dispatcher import FSMContext
from FSM.fsm import ProblemRead,Registration
from Keyboards.keyboards import *
from Connections.conn import *
from Features.function import *

async def admin(message: types.Message):
    if chek_id(message.from_user.id) != True:
        await message.answer(f"Нет доступа, воспользуйся командой /start")
    else:
        await message.answer(f'<em>Открытые тикеты</em>',reply_markup=keyboard_ticket)

async def ticket_info(call: types.CallbackQuery):
    await call.answer()
    info = jira_ticket_info(jira,call.data)
    await bot.send_message(call.from_user.id,f"{info}")

def register_handlers_admin(dp: Dispatcher):
    dp.register_message_handler(admin, commands="admin", state="*",chat_type='private')
    dp.register_callback_query_handler(ticket_info)