@echo off
set dbname="gunfunapp"
for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined MyDate set MyDate=%%x
for /f %%x in ('wmic path win32_localtime get /format:list ^| findstr "="') do set %%x
set fmonth=00%Month%
set fday=00%Day%
set today="..\_backup\%Year%-%fmonth:~-2%-%fday:~-2% DATA H2"
mkdir %today%

copy %dbname%.mv.db %today%\%dbname%.mv.db
copy %dbname%.trace.db %today%\%dbname%.trace.db