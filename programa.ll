;Programa: Prueba
source_filename = "Prueba.txt"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-windows-msvc19.16.27038"

declare i32 @printf(i8*, ...)

@.integer = private constant [4 x i8] c"%d\0A\00"

define i32 @main(i32, i8**) {
	%ptro.1 = add i32 0, 15
	%ptro.2 = add i32 0, 3
	%ptro.3 = add i32 %ptro.1, %ptro.2
	%ptro.4 = add i32 0, 20
	%ptro.5 = mul i32 %ptro.3, %ptro.4
	%ptro.6 = add i32 0, 3
	%ptro.7 = sdiv i32 %ptro.5, %ptro.6
	%ptro.8 = add i32 0, 21
	%ptro.9 = sub i32 %ptro.7, %ptro.8
	%ptro.10 = call i32 (i8*, ...) @printf(i8* getelementptr([4 x i8], [4 x i8]* @.integer, i32 0, i32 0), i32 %ptro.9)
	ret i32 0
}


