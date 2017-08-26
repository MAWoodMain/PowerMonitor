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
3409  000d ae002d        	ldw	x,#L5132
3410  0010 cd0000        	call	_sendString
3412                     ; 26 	RTC_INIT();
3414  0013 cd0000        	call	_RTC_INIT
3416                     ; 27 	sendString("RTC Initialised");
3418  0016 ae001d        	ldw	x,#L7132
3419  0019 cd0000        	call	_sendString
3421                     ; 28 	ADC_INIT();
3423  001c cd0000        	call	_ADC_INIT
3425                     ; 29 	sendString("ADC Initialised");
3427  001f ae000d        	ldw	x,#L1232
3428  0022 cd0000        	call	_sendString
3430                     ; 32 	PC_DDR |= 1 << 6;
3432  0025 721c500c      	bset	_PC_DDR,#6
3433                     ; 33 	PC_CR1 |= 1 << 6;
3435  0029 721c500d      	bset	_PC_CR1,#6
3436                     ; 35 	PC_ODR &= ~(1 << 6);
3438  002d 721d500a      	bres	_PC_ODR,#6
3439                     ; 36 }
3442  0031 81            	ret
3485                     .const:	section	.text
3486  0000               L21:
3487  0000 00000009      	dc.l	9
3488                     ; 38 void loop()
3488                     ; 39 {
3489                     	switch	.text
3490  0032               _loop:
3492  0032 5204          	subw	sp,#4
3493       00000004      OFST:	set	4
3496                     ; 42 	long i = 0;
3498  0034 ae0000        	ldw	x,#0
3499  0037 1f03          	ldw	(OFST-1,sp),x
3500  0039 ae0000        	ldw	x,#0
3501  003c 1f01          	ldw	(OFST-3,sp),x
3502  003e               L1432:
3503                     ; 46 		calcVI(VOLTAGE_CHANNEL, CHANNELS[i], 10);
3505  003e ae000a        	ldw	x,#10
3506  0041 89            	pushw	x
3507  0042 1e05          	ldw	x,(OFST+1,sp)
3508  0044 e600          	ld	a,(_CHANNELS,x)
3509  0046 97            	ld	xl,a
3510  0047 b600          	ld	a,_VOLTAGE_CHANNEL
3511  0049 95            	ld	xh,a
3512  004a cd0000        	call	_calcVI
3514  004d 85            	popw	x
3515                     ; 47 		if(i==0) 
3517  004e 96            	ldw	x,sp
3518  004f 1c0001        	addw	x,#OFST-3
3519  0052 cd0000        	call	c_lzmp
3521  0055 2614          	jrne	L7432
3522                     ; 49 			sendString("PM_START");
3524  0057 ae0004        	ldw	x,#L1532
3525  005a cd0000        	call	_sendString
3527                     ; 51 			sendFloatAsString(getVrms());
3529  005d cd0000        	call	_getVrms
3531  0060 be02          	ldw	x,c_lreg+2
3532  0062 89            	pushw	x
3533  0063 be00          	ldw	x,c_lreg
3534  0065 89            	pushw	x
3535  0066 cd0000        	call	_sendFloatAsString
3537  0069 5b04          	addw	sp,#4
3538  006b               L7432:
3539                     ; 53 		sendChar(i);
3541  006b 7b04          	ld	a,(OFST+0,sp)
3542  006d cd0000        	call	_sendChar
3544                     ; 54 		sendFloatAsString(getIrms());
3546  0070 cd0000        	call	_getIrms
3548  0073 be02          	ldw	x,c_lreg+2
3549  0075 89            	pushw	x
3550  0076 be00          	ldw	x,c_lreg
3551  0078 89            	pushw	x
3552  0079 cd0000        	call	_sendFloatAsString
3554  007c 5b04          	addw	sp,#4
3555                     ; 55 		sendFloatAsString(getRealPower());
3557  007e cd0000        	call	_getRealPower
3559  0081 be02          	ldw	x,c_lreg+2
3560  0083 89            	pushw	x
3561  0084 be00          	ldw	x,c_lreg
3562  0086 89            	pushw	x
3563  0087 cd0000        	call	_sendFloatAsString
3565  008a 5b04          	addw	sp,#4
3566                     ; 62 		i++;
3568  008c 96            	ldw	x,sp
3569  008d 1c0001        	addw	x,#OFST-3
3570  0090 a601          	ld	a,#1
3571  0092 cd0000        	call	c_lgadc
3573                     ; 44 	while(i<HARDWARE_CHANNEL_NUM)
3575  0095 9c            	rvf
3576  0096 96            	ldw	x,sp
3577  0097 1c0001        	addw	x,#OFST-3
3578  009a cd0000        	call	c_ltor
3580  009d ae0000        	ldw	x,#L21
3581  00a0 cd0000        	call	c_lcmp
3583  00a3 2f99          	jrslt	L1432
3584                     ; 64 	i = 0;
3586                     ; 67 }
3589  00a5 5b04          	addw	sp,#4
3590  00a7 81            	ret
3603                     	xdef	_main
3604                     	xdef	_loop
3605                     	xdef	_setup
3606                     	xref	_getRealPower
3607                     	xref	_getIrms
3608                     	xref	_getVrms
3609                     	xref	_calcVI
3610                     	xref	_ADC_INIT
3611                     	xref.b	_VOLTAGE_CHANNEL
3612                     	xref.b	_CHANNELS
3613                     	xref	_RTC_INIT
3614                     	xref	_sendFloatAsString
3615                     	xref	_sendString
3616                     	xref	_sendChar
3617                     	xref	_UART_INIT
3618                     	switch	.const
3619  0004               L1532:
3620  0004 504d5f535441  	dc.b	"PM_START",0
3621  000d               L1232:
3622  000d 41444320496e  	dc.b	"ADC Initialised",0
3623  001d               L7132:
3624  001d 52544320496e  	dc.b	"RTC Initialised",0
3625  002d               L5132:
3626  002d 554152542049  	dc.b	"UART Initialised",0
3627                     	xref.b	c_lreg
3647                     	xref	c_lcmp
3648                     	xref	c_ltor
3649                     	xref	c_lgadc
3650                     	xref	c_lzmp
3651                     	end
