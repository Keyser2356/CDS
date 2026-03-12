<p align="center">
  <h1 align="center">CDS</h1>
  <p align="center">
    <b>Простой плагин для отправки сообщений по клику в воздухе</b><br>
    Paper 1.21+
  </p>

  <p align="center">
    <a href="https://github.com/Keyser2356/CDS"><img src="https://img.shields.io/github/stars/Keyser2356/CDS?style=for-the-badge&color=7289da" alt="Stars"></a>
    <a href="https://github.com/Keyser2356/CDS/releases"><img src="https://img.shields.io/github/downloads/Keyser2356/CDS/total?style=for-the-badge&color=00b894" alt="Downloads"></a>
    <a href="https://papermc.io"><img src="https://img.shields.io/badge/Paper-1.21+-orange?style=for-the-badge&logo=java&logoColor=white" alt="Paper 1.21+"></a>
  </p>
</p>

---

### Что делает плагин

Плагин добавляет одну команду — `/cds`.

С её помощью можно поставить **невидимую точку** в мире.  
Когда игрок нажимает **ПКМ** по этой точке (в воздухе, без блока), ему отправляется заранее заданное сообщение в чат.

Сейчас плагин используется в основном для показа **ссылки на Discord**-сервер при клике.

Сообщение полностью настраивается в конфиге.

---

### Установка

1. Скачай [CDS.jar](https://github.com/твой_ник/CDS/releases/latest)  
2. Положи файл в папку `plugins`  
3. Перезапусти сервер  
4. Настрой `plugins/CDS/config.yml` (если нужно)  
5. Готово

---

### Конфиг (config.yml)

```yaml
metrics:
  # Отправлять анонимную статистику на bStats?
  enabled: false
  # ID плагина на bstats.org (0 = отключено)
  bstats-id: 0

commands:
  # true  →  /cds могут использовать только игроки с правами cds.admin или OP
  # false →  любой игрок может ставить точки
  cds_admin_only: true

link:
  # short →  показывает только discord.gg/xxxxxx
  # full  →  показывает полный линк https://discord.gg/xxxxxx
  type: short

message:
  # Шаблон сообщения, которое увидит игрок при клике
  # {link} — сюда подставится ссылка на дискорд
  # Поддерживаются & цветовые коды
  template: '&aПрисоединяйтесь к нашему Discord для обновлений, микро-событий и подарков &8> &6{link}'
```

---

### Как создать точку

1. Встань на нужное место (можно в воздухе)  
2. Выполни команду:  
   ```
   /cds
   ```
   (или `/cds create`, если в будущем добавишь аргументы)

Точка создаётся ровно там, где ты стоишь.  
Она невидимая и не занимает блок.

Теперь любой игрок, кто кликнет ПКМ по этой координате → получит сообщение из конфига.

---

### Права

- `cds.admin` — создание / удаление точек (если `cds_admin_only: true`)  
- `cds.use` — возможность кликать по точкам и получать сообщение (по умолчанию всем)

---

<p align="center">
  <sub>Минимум функций — максимум стабильности</sub><br>
  <sub>Плагин полностью на русском языке</sub>
</p>
