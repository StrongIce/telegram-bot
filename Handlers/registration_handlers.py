from aiogram import Dispatcher, types
from aiogram.dispatcher import FSMContext
from FSM.fsm import Registration
from Keyboards.keyboards import *
from Connections.conn import *
from Features.function import *



async def chek_access(message: types.Message,state=FSMContext):
    if chek_id(message.from_user.id) != True or message.from_user.username == None:
        await message.answer(f'Пока у тебя нет доступа! \
                            \nЗа доступом нужно обратиться к системным администраторам, \
                            либо прочитать закрепленное сообщение в нашей закрытой группе! \n'
                            f" \n" 
                            f"<b>А также: \nЗаполни поле <u>username</u> в профиле  Телеграм!</b>",
                            reply_markup=keyboard_access)
        await Registration.chek_accses.set()    
    else:
        await message.answer(f"Добрый день! \n"
                             f'Если хочешь сообщить о проблеме введи команду /open_ticket')
        await state.finish()

async def acces_word(message: types.Message,state=FSMContext):
    answers = ("У меня есть доступ!","Я не из вашей компании!")
    if message.text not in answers:
        await message.answer('Воспользуйся клавитурой',reply_markup=keyboard_access)
    if message.text == "У меня есть доступ!" and message.from_user.username != None:
        await message.answer('Введи кодовое слово:',reply_markup = types.ReplyKeyboardRemove())
        await Registration.access_word.set()
    if message.text == "Я не из вашей компании!":
        await message.answer(f"Уходи!", reply_markup=types.ReplyKeyboardRemove())
        await bot.send_sticker(message.from_user.id,"CAACAgIAAxkBAAEBdQFgzHuT_pkz_EfspbmukBIz7tnfgwACqgAD--ADAAFoUAqQLfrNfh8E")
        await state.finish()
    if message.from_user.username == None:
        await message.answer(f"<b>Заполни поле <u>username</u> в профиле  Телеграм!</b>")
    
async def first_name(message: types.Message,state=FSMContext):
    if message.text == 'Доступ21':
        await message.answer("Введи свое <em>Имя</em>: ",reply_markup = types.ReplyKeyboardRemove())
        await Registration.first_name.set()
    else:
        await message.answer('Введи кодовое слово:')
        await Registration.access_word.set()

async def last_name(message: types.Message,state=FSMContext):
    await state.update_data(first_name=message.text)
    await message.answer("Введи свою <em>Фамилию</em>: ")
    await Registration.last_name.set()


async def read_info(message: types.Message,state=FSMContext):
    last_name = message.text
    user_date = await state.get_data()
    user_date = f'{last_name} {user_date["first_name"]}'
    ldap_username = await login_ldap_search(user_date)
    if ldap_username == None:
        await message.answer(f'<em>Не знаю я таких! <u>{user_date}</u> \nА ну повтори свое имя: </em>')
        await Registration.first_name.set()
    else:
        add_user_base(user_id=message.from_user.id,telegram_username=message.from_user.username,full_name=user_date,ldap_username=ldap_username)
        await message.answer(f'Регистрация пройдена!\n' 
                        f'Если хочешь сообщить о проблеме введи команду /open_ticket')
        await state.finish()


def register_handlers_access(dp: Dispatcher):
    dp.register_message_handler(chek_access, commands="start", state="*",chat_type='private')
    dp.register_message_handler(acces_word,state=Registration.chek_accses)
    dp.register_message_handler(first_name,state=Registration.access_word)
    dp.register_message_handler(last_name,state=Registration.first_name)
    dp.register_message_handler(read_info,state=Registration.last_name)


