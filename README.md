# Aeronautics Pipe Fuel

Мод для NeoForge 1.21.1: Portable Engine из Create Aeronautics (Simulated) можно заправлять жидким
топливом по трубам Create (по умолчанию — лавой).

## Как работает
Мод не зависит от кода Aeronautics при компиляции. Он находит блоки `simulated:*portable_engine*` и
вешает на них жидкостную capability. Жидкость копится в буфере; когда набирается порция
(1000 mB лавы), в слот топлива двигателя кладётся лавовое ведро, и двигатель сжигает его как обычно.
Если слот занят, буфер заполняется и труба останавливается. Подключайте трубу к любой стороне двигателя.

## Свои жидкости (датапак)
`data/<любой_namespace>/data_maps/fluid/engine_fuel.json`:
```json
{
  "values": {
    "minecraft:lava": { "item": "minecraft:lava_bucket", "amount": 1000 },
    "#c:biofuel": { "item": "minecraft:coal", "amount": 250 }
  }
}
```
`item` — предмет, который двигатель сожжёт (время горения берётся с него), `amount` — mB жидкости на один такой предмет.

## Сборка
Залить репозиторий на GitHub — Actions соберёт jar (артефакт `aeropipefuel-jar`).
Локально: `gradle build` (Java 21, Gradle 8.10+).

## Совместимость
Если установлен «Create Aeronautics: Portable Engine Liquid Fuel», оба мода дают двигателю жидкостный вход — оставьте один.
