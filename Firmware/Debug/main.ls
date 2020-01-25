   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.11.14 - 18 Nov 2019
   3                     ; Generator (Limited) V4.4.11 - 19 Nov 2019
3345                     ; 11 main()
3345                     ; 12 {
3347                     	switch	.text
3348  0000               _main:
3352                     ; 13 	setup();
3354  0000 ad04          	call	_setup
3356  0002               L1032:
3357                     ; 15 		loop();
3359  0002 ad2e          	call	_loop
3362  0004 20fc          	jra	L1032
3393                     ; 20 void setup()
3393                     ; 21 {	
3394                     	switch	.text
3395  0006               _setup:
3399                     ; 22 	CLK_CKDIVR = 0x00;
3401  0006 725f50c0      	clr	_CLK_CKDIVR
3402                     ; 24 	UART_INIT();
3404  000a cd0000        	call	_UART_INIT
3406                     ; 25 	sendString("UART Initialised");
3408  000d ae0029        	ldw	x,#L5132
3409  0010 cd0000        	call	_sendString
3411                     ; 26 	RTC_INIT();
3413  0013 cd0000        	call	_RTC_INIT
3415                     ; 27 	sendString("RTC Initialised");
3417  0016 ae0019        	ldw	x,#L7132
3418  0019 cd0000        	call	_sendString
3420                     ; 28 	ADC_INIT();
3422  001c cd0000        	call	_ADC_INIT
3424                     ; 29 	sendString("ADC Initialised");
3426  001f ae0009        	ldw	x,#L1232
3427  0022 cd0000        	call	_sendString
3429                     ; 32 	PC_DDR |= 1 << 6;
3431  0025 721c500c      	bset	_PC_DDR,#6
3432                     ; 33 	PC_CR1 |= 1 << 6;
3434  0029 721c500d      	bset	_PC_CR1,#6
3435                     ; 35 	PC_ODR &= ~(1 << 6);
3437  002d 721d500a      	bres	_PC_ODR,#6
3438                     ; 36 }
3441  0031 81            	ret
3483                     ; 38 void loop()
3483                     ; 39 {
3484                     	switch	.text
3485  0032               _loop:
3487  0032 89            	pushw	x
3488       00000002      OFST:	set	2
3491                     ; 42 	int i = 0;
3493  0033 5f            	clrw	x
3494  0034 1f01          	ldw	(OFST-1,sp),x
3496                     ; 43 	PC_ODR |= 1 << 6;
3498  0036 721c500a      	bset	_PC_ODR,#6
3499                     ; 44 	calcVI(8);
3501  003a ae0008        	ldw	x,#8
3502  003d cd0000        	call	_calcVI
3504                     ; 45 	PC_ODR &= ~(1 << 6);
3506  0040 721d500a      	bres	_PC_ODR,#6
3507                     ; 46 	sendString("PM_START");
3509  0044 ae0000        	ldw	x,#L1432
3510  0047 cd0000        	call	_sendString
3512                     ; 47 	sendFloatAsString(getVrms());
3514  004a cd0000        	call	_getVrms
3516  004d be02          	ldw	x,c_lreg+2
3517  004f 89            	pushw	x
3518  0050 be00          	ldw	x,c_lreg
3519  0052 89            	pushw	x
3520  0053 cd0000        	call	_sendFloatAsString
3522  0056 5b04          	addw	sp,#4
3524  0058 202d          	jra	L5432
3525  005a               L3432:
3526                     ; 51 		sendChar(i+1);
3528  005a 7b02          	ld	a,(OFST+0,sp)
3529  005c 4c            	inc	a
3530  005d cd0000        	call	_sendChar
3532                     ; 52 		sendFloatAsString(getIrms(i));
3534  0060 1e01          	ldw	x,(OFST-1,sp)
3535  0062 cd0000        	call	_getIrms
3537  0065 be02          	ldw	x,c_lreg+2
3538  0067 89            	pushw	x
3539  0068 be00          	ldw	x,c_lreg
3540  006a 89            	pushw	x
3541  006b cd0000        	call	_sendFloatAsString
3543  006e 5b04          	addw	sp,#4
3544                     ; 53 		sendFloatAsString(getRealPower(i));
3546  0070 1e01          	ldw	x,(OFST-1,sp)
3547  0072 cd0000        	call	_getRealPower
3549  0075 be02          	ldw	x,c_lreg+2
3550  0077 89            	pushw	x
3551  0078 be00          	ldw	x,c_lreg
3552  007a 89            	pushw	x
3553  007b cd0000        	call	_sendFloatAsString
3555  007e 5b04          	addw	sp,#4
3556                     ; 54 		i++;
3558  0080 1e01          	ldw	x,(OFST-1,sp)
3559  0082 1c0001        	addw	x,#1
3560  0085 1f01          	ldw	(OFST-1,sp),x
3562  0087               L5432:
3563                     ; 49 	while(i<HARDWARE_CHANNEL_NUM)
3565  0087 9c            	rvf
3566  0088 1e01          	ldw	x,(OFST-1,sp)
3567  008a a30009        	cpw	x,#9
3568  008d 2fcb          	jrslt	L3432
3569                     ; 56 	i = 0;
3571                     ; 57 }
3574  008f 85            	popw	x
3575  0090 81            	ret
3588                     	xdef	_main
3589                     	xdef	_loop
3590                     	xdef	_setup
3591                     	xref	_getRealPower
3592                     	xref	_getIrms
3593                     	xref	_getVrms
3594                     	xref	_calcVI
3595                     	xref	_ADC_INIT
3596                     	xref	_RTC_INIT
3597                     	xref	_sendFloatAsString
3598                     	xref	_sendString
3599                     	xref	_sendChar
3600                     	xref	_UART_INIT
3601                     .const:	section	.text
3602  0000               L1432:
3603  0000 504d5f535441  	dc.b	"PM_START",0
3604  0009               L1232:
3605  0009 41444320496e  	dc.b	"ADC Initialised",0
3606  0019               L7132:
3607  0019 52544320496e  	dc.b	"RTC Initialised",0
3608  0029               L5132:
3609  0029 554152542049  	dc.b	"UART Initialised",0
3610                     	xref.b	c_lreg
3630                     	end
