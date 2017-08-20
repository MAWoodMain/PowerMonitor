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
3360  0002 ad43          	call	_loop
3363  0004 20fc          	jra	L1032
3404                     ; 20 void setup()
3404                     ; 21 {
3405                     	switch	.text
3406  0006               _setup:
3408  0006 5204          	subw	sp,#4
3409       00000004      OFST:	set	4
3412                     ; 22 	double d = 7.5;
3414  0008 ce003b        	ldw	x,L7232+2
3415  000b 1f03          	ldw	(OFST-1,sp),x
3416  000d ce0039        	ldw	x,L7232
3417  0010 1f01          	ldw	(OFST-3,sp),x
3418                     ; 24 	CLK_CKDIVR = 0x00;
3420  0012 725f50c0      	clr	_CLK_CKDIVR
3421                     ; 26 	UART_INIT();
3423  0016 cd0000        	call	_UART_INIT
3425                     ; 27 	sendString("UART Initialised");
3427  0019 ae0028        	ldw	x,#L3332
3428  001c cd0000        	call	_sendString
3430                     ; 28 	RTC_INIT();
3432  001f cd0000        	call	_RTC_INIT
3434                     ; 29 	sendString("RTC Initialised");
3436  0022 ae0018        	ldw	x,#L5332
3437  0025 cd0000        	call	_sendString
3439                     ; 30 	ADC_INIT();
3441  0028 cd0000        	call	_ADC_INIT
3443                     ; 31 	sendString("ADC Initialised");
3445  002b ae0008        	ldw	x,#L7332
3446  002e cd0000        	call	_sendString
3448                     ; 33 	sendChar(0x00);
3450  0031 4f            	clr	a
3451  0032 cd0000        	call	_sendChar
3453                     ; 34 	sendDouble(d);
3455  0035 1e03          	ldw	x,(OFST-1,sp)
3456  0037 89            	pushw	x
3457  0038 1e03          	ldw	x,(OFST-1,sp)
3458  003a 89            	pushw	x
3459  003b cd0000        	call	_sendDouble
3461  003e 5b04          	addw	sp,#4
3462                     ; 35 	sendChar(0x00);
3464  0040 4f            	clr	a
3465  0041 cd0000        	call	_sendChar
3467                     ; 36 }
3470  0044 5b04          	addw	sp,#4
3471  0046 81            	ret
3522                     .const:	section	.text
3523  0000               L21:
3524  0000 00000009      	dc.l	9
3525  0004               L41:
3526  0004 0000b960      	dc.l	47456
3527                     ; 38 void loop()
3527                     ; 39 {
3528                     	switch	.text
3529  0047               _loop:
3531  0047 5206          	subw	sp,#6
3532       00000006      OFST:	set	6
3535                     ; 40 	unsigned long i = 0;
3537  0049 ae0000        	ldw	x,#0
3538  004c 1f05          	ldw	(OFST-1,sp),x
3539  004e ae0000        	ldw	x,#0
3540  0051 1f03          	ldw	(OFST-3,sp),x
3541                     ; 41 	unsigned int adcValue = 0;
3543                     ; 42 	adcValue = readChannel(VOLTAGE_CHANNEL);
3545  0053 b600          	ld	a,_VOLTAGE_CHANNEL
3546  0055 5f            	clrw	x
3547  0056 97            	ld	xl,a
3548  0057 cd0000        	call	_readChannel
3550  005a 1f01          	ldw	(OFST-5,sp),x
3551                     ; 43 	sendChar('V');
3553  005c a656          	ld	a,#86
3554  005e cd0000        	call	_sendChar
3556                     ; 44 	sendChar((char)(adcValue >> 8));
3558  0061 7b01          	ld	a,(OFST-5,sp)
3559  0063 cd0000        	call	_sendChar
3561                     ; 45 	sendChar((char)adcValue);
3563  0066 7b02          	ld	a,(OFST-4,sp)
3564  0068 cd0000        	call	_sendChar
3567  006b 203a          	jra	L5632
3568  006d               L3632:
3569                     ; 48 		calcVI(VOLTAGE_CHANNEL, CHANNELS[i], 30);
3571  006d ae001e        	ldw	x,#30
3572  0070 89            	pushw	x
3573  0071 1e07          	ldw	x,(OFST+1,sp)
3574  0073 e600          	ld	a,(_CHANNELS,x)
3575  0075 97            	ld	xl,a
3576  0076 b600          	ld	a,_VOLTAGE_CHANNEL
3577  0078 95            	ld	xh,a
3578  0079 cd0000        	call	_calcVI
3580  007c 85            	popw	x
3581                     ; 49 		sendChar(i);
3583  007d 7b06          	ld	a,(OFST+0,sp)
3584  007f cd0000        	call	_sendChar
3586                     ; 50 		sendDouble(getApparentPower());
3588  0082 cd0000        	call	_getApparentPower
3590  0085 be02          	ldw	x,c_lreg+2
3591  0087 89            	pushw	x
3592  0088 be00          	ldw	x,c_lreg
3593  008a 89            	pushw	x
3594  008b cd0000        	call	_sendDouble
3596  008e 5b04          	addw	sp,#4
3597                     ; 51 		sendDouble(getRealPower());
3599  0090 cd0000        	call	_getRealPower
3601  0093 be02          	ldw	x,c_lreg+2
3602  0095 89            	pushw	x
3603  0096 be00          	ldw	x,c_lreg
3604  0098 89            	pushw	x
3605  0099 cd0000        	call	_sendDouble
3607  009c 5b04          	addw	sp,#4
3608                     ; 52 		i++;
3610  009e 96            	ldw	x,sp
3611  009f 1c0003        	addw	x,#OFST-3
3612  00a2 a601          	ld	a,#1
3613  00a4 cd0000        	call	c_lgadc
3615  00a7               L5632:
3616                     ; 46 	while(i<HARDWARE_CHANNEL_NUM)
3618  00a7 96            	ldw	x,sp
3619  00a8 1c0003        	addw	x,#OFST-3
3620  00ab cd0000        	call	c_ltor
3622  00ae ae0000        	ldw	x,#L21
3623  00b1 cd0000        	call	c_lcmp
3625  00b4 25b7          	jrult	L3632
3626                     ; 54 	i = 0;
3628  00b6 ae0000        	ldw	x,#0
3629  00b9 1f05          	ldw	(OFST-1,sp),x
3630  00bb ae0000        	ldw	x,#0
3631  00be 1f03          	ldw	(OFST-3,sp),x
3632  00c0               L1732:
3633                     ; 55 	while(i < 47456) i++;
3636  00c0 96            	ldw	x,sp
3637  00c1 1c0003        	addw	x,#OFST-3
3638  00c4 a601          	ld	a,#1
3639  00c6 cd0000        	call	c_lgadc
3643  00c9 96            	ldw	x,sp
3644  00ca 1c0003        	addw	x,#OFST-3
3645  00cd cd0000        	call	c_ltor
3647  00d0 ae0004        	ldw	x,#L41
3648  00d3 cd0000        	call	c_lcmp
3650  00d6 25e8          	jrult	L1732
3652  00d8 2009          	jra	L1042
3653  00da               L7732:
3654                     ; 56 	while(i > 0) i--;
3656  00da 96            	ldw	x,sp
3657  00db 1c0003        	addw	x,#OFST-3
3658  00de a601          	ld	a,#1
3659  00e0 cd0000        	call	c_lgsbc
3661  00e3               L1042:
3664  00e3 96            	ldw	x,sp
3665  00e4 1c0003        	addw	x,#OFST-3
3666  00e7 cd0000        	call	c_lzmp
3668  00ea 26ee          	jrne	L7732
3669                     ; 57 }
3672  00ec 5b06          	addw	sp,#6
3673  00ee 81            	ret
3686                     	xdef	_main
3687                     	xdef	_loop
3688                     	xdef	_setup
3689                     	xref	_getRealPower
3690                     	xref	_getApparentPower
3691                     	xref	_calcVI
3692                     	xref	_readChannel
3693                     	xref	_ADC_INIT
3694                     	xref.b	_VOLTAGE_CHANNEL
3695                     	xref.b	_CHANNELS
3696                     	xref	_RTC_INIT
3697                     	xref	_sendString
3698                     	xref	_sendChar
3699                     	xref	_sendDouble
3700                     	xref	_UART_INIT
3701                     	switch	.const
3702  0008               L7332:
3703  0008 41444320496e  	dc.b	"ADC Initialised",0
3704  0018               L5332:
3705  0018 52544320496e  	dc.b	"RTC Initialised",0
3706  0028               L3332:
3707  0028 554152542049  	dc.b	"UART Initialised",0
3708  0039               L7232:
3709  0039 40f00000      	dc.w	16624,0
3710                     	xref.b	c_lreg
3730                     	xref	c_lzmp
3731                     	xref	c_lgsbc
3732                     	xref	c_lcmp
3733                     	xref	c_ltor
3734                     	xref	c_lgadc
3735                     	end
