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
3523                     .const:	section	.text
3524  0000               L21:
3525  0000 00000009      	dc.l	9
3526  0004               L41:
3527  0004 0000b960      	dc.l	47456
3528                     ; 38 void loop()
3528                     ; 39 {
3529                     	switch	.text
3530  0047               _loop:
3532  0047 5206          	subw	sp,#6
3533       00000006      OFST:	set	6
3536                     ; 40 	unsigned long i = 0;
3538  0049 ae0000        	ldw	x,#0
3539  004c 1f05          	ldw	(OFST-1,sp),x
3540  004e ae0000        	ldw	x,#0
3541  0051 1f03          	ldw	(OFST-3,sp),x
3542                     ; 41 	unsigned int adcValue = 0;
3544                     ; 42 	adcValue = readChannel(VOLTAGE_CHANNEL);
3546  0053 b600          	ld	a,_VOLTAGE_CHANNEL
3547  0055 5f            	clrw	x
3548  0056 97            	ld	xl,a
3549  0057 cd0000        	call	_readChannel
3551                     ; 43 	sendChar('V');
3553  005a a656          	ld	a,#86
3554  005c cd0000        	call	_sendChar
3557  005f 2051          	jra	L5632
3558  0061               L3632:
3559                     ; 48 		calcVI(VOLTAGE_CHANNEL, CHANNELS[i], 30);
3561  0061 ae001e        	ldw	x,#30
3562  0064 89            	pushw	x
3563  0065 1e07          	ldw	x,(OFST+1,sp)
3564  0067 e600          	ld	a,(_CHANNELS,x)
3565  0069 97            	ld	xl,a
3566  006a b600          	ld	a,_VOLTAGE_CHANNEL
3567  006c 95            	ld	xh,a
3568  006d cd0000        	call	_calcVI
3570  0070 85            	popw	x
3571                     ; 49 		if(i==0) sendDouble(getVrms());
3573  0071 96            	ldw	x,sp
3574  0072 1c0003        	addw	x,#OFST-3
3575  0075 cd0000        	call	c_lzmp
3577  0078 260e          	jrne	L1732
3580  007a cd0000        	call	_getVrms
3582  007d be02          	ldw	x,c_lreg+2
3583  007f 89            	pushw	x
3584  0080 be00          	ldw	x,c_lreg
3585  0082 89            	pushw	x
3586  0083 cd0000        	call	_sendDouble
3588  0086 5b04          	addw	sp,#4
3589  0088               L1732:
3590                     ; 50 		sendChar(i);
3592  0088 7b06          	ld	a,(OFST+0,sp)
3593  008a cd0000        	call	_sendChar
3595                     ; 51 		sendDouble(getApparentPower());
3597  008d cd0000        	call	_getApparentPower
3599  0090 be02          	ldw	x,c_lreg+2
3600  0092 89            	pushw	x
3601  0093 be00          	ldw	x,c_lreg
3602  0095 89            	pushw	x
3603  0096 cd0000        	call	_sendDouble
3605  0099 5b04          	addw	sp,#4
3606                     ; 52 		sendDouble(getRealPower());
3608  009b cd0000        	call	_getRealPower
3610  009e be02          	ldw	x,c_lreg+2
3611  00a0 89            	pushw	x
3612  00a1 be00          	ldw	x,c_lreg
3613  00a3 89            	pushw	x
3614  00a4 cd0000        	call	_sendDouble
3616  00a7 5b04          	addw	sp,#4
3617                     ; 53 		i++;
3619  00a9 96            	ldw	x,sp
3620  00aa 1c0003        	addw	x,#OFST-3
3621  00ad a601          	ld	a,#1
3622  00af cd0000        	call	c_lgadc
3624  00b2               L5632:
3625                     ; 46 	while(i<HARDWARE_CHANNEL_NUM)
3627  00b2 96            	ldw	x,sp
3628  00b3 1c0003        	addw	x,#OFST-3
3629  00b6 cd0000        	call	c_ltor
3631  00b9 ae0000        	ldw	x,#L21
3632  00bc cd0000        	call	c_lcmp
3634  00bf 25a0          	jrult	L3632
3635                     ; 55 	i = 0;
3637  00c1 ae0000        	ldw	x,#0
3638  00c4 1f05          	ldw	(OFST-1,sp),x
3639  00c6 ae0000        	ldw	x,#0
3640  00c9 1f03          	ldw	(OFST-3,sp),x
3641  00cb               L3732:
3642                     ; 56 	while(i < 47456) i++;
3645  00cb 96            	ldw	x,sp
3646  00cc 1c0003        	addw	x,#OFST-3
3647  00cf a601          	ld	a,#1
3648  00d1 cd0000        	call	c_lgadc
3652  00d4 96            	ldw	x,sp
3653  00d5 1c0003        	addw	x,#OFST-3
3654  00d8 cd0000        	call	c_ltor
3656  00db ae0004        	ldw	x,#L41
3657  00de cd0000        	call	c_lcmp
3659  00e1 25e8          	jrult	L3732
3661  00e3 2009          	jra	L3042
3662  00e5               L1042:
3663                     ; 57 	while(i > 0) i--;
3665  00e5 96            	ldw	x,sp
3666  00e6 1c0003        	addw	x,#OFST-3
3667  00e9 a601          	ld	a,#1
3668  00eb cd0000        	call	c_lgsbc
3670  00ee               L3042:
3673  00ee 96            	ldw	x,sp
3674  00ef 1c0003        	addw	x,#OFST-3
3675  00f2 cd0000        	call	c_lzmp
3677  00f5 26ee          	jrne	L1042
3678                     ; 58 }
3681  00f7 5b06          	addw	sp,#6
3682  00f9 81            	ret
3695                     	xdef	_main
3696                     	xdef	_loop
3697                     	xdef	_setup
3698                     	xref	_getVrms
3699                     	xref	_getRealPower
3700                     	xref	_getApparentPower
3701                     	xref	_calcVI
3702                     	xref	_readChannel
3703                     	xref	_ADC_INIT
3704                     	xref.b	_VOLTAGE_CHANNEL
3705                     	xref.b	_CHANNELS
3706                     	xref	_RTC_INIT
3707                     	xref	_sendString
3708                     	xref	_sendChar
3709                     	xref	_sendDouble
3710                     	xref	_UART_INIT
3711                     	switch	.const
3712  0008               L7332:
3713  0008 41444320496e  	dc.b	"ADC Initialised",0
3714  0018               L5332:
3715  0018 52544320496e  	dc.b	"RTC Initialised",0
3716  0028               L3332:
3717  0028 554152542049  	dc.b	"UART Initialised",0
3718  0039               L7232:
3719  0039 40f00000      	dc.w	16624,0
3720                     	xref.b	c_lreg
3740                     	xref	c_lgsbc
3741                     	xref	c_lcmp
3742                     	xref	c_ltor
3743                     	xref	c_lgadc
3744                     	xref	c_lzmp
3745                     	end
