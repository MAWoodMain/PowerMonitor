   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3346                     ; 11 main()
3346                     ; 12 {
3348                     	switch	.text
3349  0000               _main:
3353                     ; 13 	setup();
3355  0000 ad04          	call	_setup
3357  0002               L1032:
3358                     ; 15 		loop();
3360  0002 ad2e          	call	_loop
3363  0004 20fc          	jra	L1032
3394                     ; 20 void setup()
3394                     ; 21 {	
3395                     	switch	.text
3396  0006               _setup:
3400                     ; 22 	CLK_CKDIVR = 0x00;
3402  0006 725f50c0      	clr	_CLK_CKDIVR
3403                     ; 24 	UART_INIT();
3405  000a cd0000        	call	_UART_INIT
3407                     ; 25 	sendString("UART Initialised");
3409  000d ae0029        	ldw	x,#L5132
3410  0010 cd0000        	call	_sendString
3412                     ; 26 	RTC_INIT();
3414  0013 cd0000        	call	_RTC_INIT
3416                     ; 27 	sendString("RTC Initialised");
3418  0016 ae0019        	ldw	x,#L7132
3419  0019 cd0000        	call	_sendString
3421                     ; 28 	ADC_INIT();
3423  001c cd0000        	call	_ADC_INIT
3425                     ; 29 	sendString("ADC Initialised");
3427  001f ae0009        	ldw	x,#L1232
3428  0022 cd0000        	call	_sendString
3430                     ; 32 	PC_DDR |= 1 << 6;
3432  0025 721c500c      	bset	_PC_DDR,#6
3433                     ; 33 	PC_CR1 |= 1 << 6;
3435  0029 721c500d      	bset	_PC_CR1,#6
3436                     ; 35 	PC_ODR &= ~(1 << 6);
3438  002d 721d500a      	bres	_PC_ODR,#6
3439                     ; 36 }
3442  0031 81            	ret
3484                     ; 38 void loop()
3484                     ; 39 {
3485                     	switch	.text
3486  0032               _loop:
3488  0032 89            	pushw	x
3489       00000002      OFST:	set	2
3492                     ; 42 	int i = 0;
3494  0033 5f            	clrw	x
3495  0034 1f01          	ldw	(OFST-1,sp),x
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
3561  0087               L5432:
3562                     ; 49 	while(i<HARDWARE_CHANNEL_NUM)
3564  0087 9c            	rvf
3565  0088 1e01          	ldw	x,(OFST-1,sp)
3566  008a a30009        	cpw	x,#9
3567  008d 2fcb          	jrslt	L3432
3568                     ; 56 	i = 0;
3570                     ; 57 }
3573  008f 85            	popw	x
3574  0090 81            	ret
3587                     	xdef	_main
3588                     	xdef	_loop
3589                     	xdef	_setup
3590                     	xref	_getRealPower
3591                     	xref	_getIrms
3592                     	xref	_getVrms
3593                     	xref	_calcVI
3594                     	xref	_ADC_INIT
3595                     	xref	_RTC_INIT
3596                     	xref	_sendFloatAsString
3597                     	xref	_sendString
3598                     	xref	_sendChar
3599                     	xref	_UART_INIT
3600                     .const:	section	.text
3601  0000               L1432:
3602  0000 504d5f535441  	dc.b	"PM_START",0
3603  0009               L1232:
3604  0009 41444320496e  	dc.b	"ADC Initialised",0
3605  0019               L7132:
3606  0019 52544320496e  	dc.b	"RTC Initialised",0
3607  0029               L5132:
3608  0029 554152542049  	dc.b	"UART Initialised",0
3609                     	xref.b	c_lreg
3629                     	end
