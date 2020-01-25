   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.11.14 - 18 Nov 2019
   3                     ; Generator (Limited) V4.4.11 - 19 Nov 2019
3350                     ; 3 void UART_INIT()
3350                     ; 4 {
3352                     	switch	.text
3353  0000               _UART_INIT:
3357                     ; 6 	CLK_PCKENR1 |= 0x20;
3359  0000 721a50c3      	bset	_CLK_PCKENR1,#5
3360                     ; 9 	PC_DDR |= 1 << 5;
3362  0004 721a500c      	bset	_PC_DDR,#5
3363                     ; 10 	PC_CR1 |= 1 << 5;
3365  0008 721a500d      	bset	_PC_CR1,#5
3366                     ; 13 	USART1_CR2 = USART_CR2_TEN;
3368  000c 35085235      	mov	_USART1_CR2,#8
3369                     ; 16 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3371  0010 c65236        	ld	a,_USART1_CR3
3372  0013 a4cf          	and	a,#207
3373  0015 c75236        	ld	_USART1_CR3,a
3374                     ; 19 	USART1_BRR2 = 0x05; 
3376  0018 35055233      	mov	_USART1_BRR2,#5
3377                     ; 20 	USART1_BRR1 = 0x04;
3379  001c 35045232      	mov	_USART1_BRR1,#4
3380                     ; 21 }
3383  0020 81            	ret
3419                     ; 23 void sendChar(unsigned char c)
3419                     ; 24 {
3420                     	switch	.text
3421  0021               _sendChar:
3423  0021 88            	push	a
3424       00000000      OFST:	set	0
3427  0022               L1232:
3428                     ; 25 	while(!(USART1_SR & USART_SR_TXE));
3430  0022 c65230        	ld	a,_USART1_SR
3431  0025 a580          	bcp	a,#128
3432  0027 27f9          	jreq	L1232
3433                     ; 26 	USART1_DR = c;
3435  0029 7b01          	ld	a,(OFST+1,sp)
3436  002b c75231        	ld	_USART1_DR,a
3437                     ; 27 }
3440  002e 84            	pop	a
3441  002f 81            	ret
3487                     ; 29 void sendString(const char *str) 
3487                     ; 30 {
3488                     	switch	.text
3489  0030               _sendString:
3491  0030 89            	pushw	x
3492  0031 88            	push	a
3493       00000001      OFST:	set	1
3496                     ; 32 	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
3498  0032 0f01          	clr	(OFST+0,sp)
3501  0034 200c          	jra	L3532
3502  0036               L7432:
3505  0036 7b01          	ld	a,(OFST+0,sp)
3506  0038 5f            	clrw	x
3507  0039 97            	ld	xl,a
3508  003a 72fb02        	addw	x,(OFST+1,sp)
3509  003d f6            	ld	a,(x)
3510  003e ade1          	call	_sendChar
3514  0040 0c01          	inc	(OFST+0,sp)
3516  0042               L3532:
3519  0042 1e02          	ldw	x,(OFST+1,sp)
3520  0044 cd0000        	call	_strlen
3522  0047 7b01          	ld	a,(OFST+0,sp)
3523  0049 905f          	clrw	y
3524  004b 9097          	ld	yl,a
3525  004d 90bf00        	ldw	c_y,y
3526  0050 b300          	cpw	x,c_y
3527  0052 22e2          	jrugt	L7432
3528                     ; 33 }
3531  0054 5b03          	addw	sp,#3
3532  0056 81            	ret
3587                     ; 35 void sendDouble(double double_value)
3587                     ; 36 {
3588                     	switch	.text
3589  0057               _sendDouble:
3591  0057 5209          	subw	sp,#9
3592       00000009      OFST:	set	9
3595                     ; 39 	memcpy(bytes, (unsigned char*) (&double_value), 8);
3597  0059 96            	ldw	x,sp
3598  005a 1c0001        	addw	x,#OFST-8
3599  005d bf00          	ldw	c_x,x
3600  005f 9096          	ldw	y,sp
3601  0061 72a9000c      	addw	y,#OFST+3
3602  0065 90bf00        	ldw	c_y,y
3603  0068 ae0008        	ldw	x,#8
3604  006b               L41:
3605  006b 5a            	decw	x
3606  006c 92d600        	ld	a,([c_y.w],x)
3607  006f 92d700        	ld	([c_x.w],x),a
3608  0072 5d            	tnzw	x
3609  0073 26f6          	jrne	L41
3610                     ; 40 	for(i = 0; i < 8; i++) sendChar(bytes[i]);
3612  0075 0f09          	clr	(OFST+0,sp)
3614  0077               L5042:
3617  0077 96            	ldw	x,sp
3618  0078 1c0001        	addw	x,#OFST-8
3619  007b 9f            	ld	a,xl
3620  007c 5e            	swapw	x
3621  007d 1b09          	add	a,(OFST+0,sp)
3622  007f 2401          	jrnc	L61
3623  0081 5c            	incw	x
3624  0082               L61:
3625  0082 02            	rlwa	x,a
3626  0083 f6            	ld	a,(x)
3627  0084 ad9b          	call	_sendChar
3631  0086 0c09          	inc	(OFST+0,sp)
3635  0088 7b09          	ld	a,(OFST+0,sp)
3636  008a a108          	cp	a,#8
3637  008c 25e9          	jrult	L5042
3638                     ; 41 }
3641  008e 5b09          	addw	sp,#9
3642  0090 81            	ret
3686                     ; 43 void sendFloatAsLong(float double_value)
3686                     ; 44 {
3687                     	switch	.text
3688  0091               _sendFloatAsLong:
3690  0091 5204          	subw	sp,#4
3691       00000004      OFST:	set	4
3694                     ; 46 	longValue = (double_value*1000000.0);
3696  0093 96            	ldw	x,sp
3697  0094 1c0007        	addw	x,#OFST+3
3698  0097 cd0000        	call	c_ltor
3700  009a ae0003        	ldw	x,#L1442
3701  009d cd0000        	call	c_fmul
3703  00a0 cd0000        	call	c_ftol
3705  00a3 96            	ldw	x,sp
3706  00a4 1c0001        	addw	x,#OFST-3
3707  00a7 cd0000        	call	c_rtol
3710                     ; 47 	sendLong(longValue);
3712  00aa 1e03          	ldw	x,(OFST-1,sp)
3713  00ac 89            	pushw	x
3714  00ad 1e03          	ldw	x,(OFST-1,sp)
3715  00af 89            	pushw	x
3716  00b0 ad05          	call	_sendLong
3718  00b2 5b04          	addw	sp,#4
3719                     ; 48 }
3722  00b4 5b04          	addw	sp,#4
3723  00b6 81            	ret
3767                     ; 50 void sendLong(unsigned long long_value)
3767                     ; 51 {
3768                     	switch	.text
3769  00b7               _sendLong:
3771  00b7 89            	pushw	x
3772       00000002      OFST:	set	2
3775                     ; 53 	for(i = 24; i >=0; i = i-8)
3777  00b8 ae0018        	ldw	x,#24
3778  00bb 1f01          	ldw	(OFST-1,sp),x
3780  00bd               L7642:
3781                     ; 55 		sendChar((long_value >> i) & 0xFF);
3783  00bd 96            	ldw	x,sp
3784  00be 1c0005        	addw	x,#OFST+3
3785  00c1 cd0000        	call	c_ltor
3787  00c4 7b02          	ld	a,(OFST+0,sp)
3788  00c6 cd0000        	call	c_lursh
3790  00c9 3f02          	clr	c_lreg+2
3791  00cb 3f01          	clr	c_lreg+1
3792  00cd 3f00          	clr	c_lreg
3793  00cf b603          	ld	a,c_lreg+3
3794  00d1 cd0021        	call	_sendChar
3796                     ; 53 	for(i = 24; i >=0; i = i-8)
3798  00d4 1e01          	ldw	x,(OFST-1,sp)
3799  00d6 1d0008        	subw	x,#8
3800  00d9 1f01          	ldw	(OFST-1,sp),x
3804  00db 9c            	rvf
3805  00dc 1e01          	ldw	x,(OFST-1,sp)
3806  00de 2edd          	jrsge	L7642
3807                     ; 57 }
3810  00e0 85            	popw	x
3811  00e1 81            	ret
3866                     ; 59 void sendFloat(float float_value)
3866                     ; 60 {
3867                     	switch	.text
3868  00e2               _sendFloat:
3870  00e2 5205          	subw	sp,#5
3871       00000005      OFST:	set	5
3874                     ; 63 	memcpy(bytes, (unsigned char*) (&float_value), 4);
3876  00e4 96            	ldw	x,sp
3877  00e5 1c0001        	addw	x,#OFST-4
3878  00e8 bf00          	ldw	c_x,x
3879  00ea 9096          	ldw	y,sp
3880  00ec 72a90008      	addw	y,#OFST+3
3881  00f0 90bf00        	ldw	c_y,y
3882  00f3 ae0004        	ldw	x,#4
3883  00f6               L62:
3884  00f6 5a            	decw	x
3885  00f7 92d600        	ld	a,([c_y.w],x)
3886  00fa 92d700        	ld	([c_x.w],x),a
3887  00fd 5d            	tnzw	x
3888  00fe 26f6          	jrne	L62
3889                     ; 64 	for(i = 0; i < 4; i++) sendChar(bytes[i]);
3891  0100 0f05          	clr	(OFST+0,sp)
3893  0102               L3252:
3896  0102 96            	ldw	x,sp
3897  0103 1c0001        	addw	x,#OFST-4
3898  0106 9f            	ld	a,xl
3899  0107 5e            	swapw	x
3900  0108 1b05          	add	a,(OFST+0,sp)
3901  010a 2401          	jrnc	L03
3902  010c 5c            	incw	x
3903  010d               L03:
3904  010d 02            	rlwa	x,a
3905  010e f6            	ld	a,(x)
3906  010f cd0021        	call	_sendChar
3910  0112 0c05          	inc	(OFST+0,sp)
3914  0114 7b05          	ld	a,(OFST+0,sp)
3915  0116 a104          	cp	a,#4
3916  0118 25e8          	jrult	L3252
3917                     ; 65 }
3920  011a 5b05          	addw	sp,#5
3921  011c 81            	ret
3976                     ; 67 void sendFloatAsString(float float_value)
3976                     ; 68 {
3977                     	switch	.text
3978  011d               _sendFloatAsString:
3980  011d 520e          	subw	sp,#14
3981       0000000e      OFST:	set	14
3984                     ; 71 	for(i = 0; i< 10; i++) output[i] = 0x30;
3986  011f 5f            	clrw	x
3987  0120 1f0d          	ldw	(OFST-1,sp),x
3989  0122               L7552:
3992  0122 96            	ldw	x,sp
3993  0123 1c0003        	addw	x,#OFST-11
3994  0126 1f01          	ldw	(OFST-13,sp),x
3996  0128 1e0d          	ldw	x,(OFST-1,sp)
3997  012a 72fb01        	addw	x,(OFST-13,sp)
3998  012d a630          	ld	a,#48
3999  012f f7            	ld	(x),a
4002  0130 1e0d          	ldw	x,(OFST-1,sp)
4003  0132 1c0001        	addw	x,#1
4004  0135 1f0d          	ldw	(OFST-1,sp),x
4008  0137 9c            	rvf
4009  0138 1e0d          	ldw	x,(OFST-1,sp)
4010  013a a3000a        	cpw	x,#10
4011  013d 2fe3          	jrslt	L7552
4012                     ; 72 	sprintf(output,"%f",float_value);
4014  013f 1e13          	ldw	x,(OFST+5,sp)
4015  0141 89            	pushw	x
4016  0142 1e13          	ldw	x,(OFST+5,sp)
4017  0144 89            	pushw	x
4018  0145 ae0000        	ldw	x,#L5652
4019  0148 89            	pushw	x
4020  0149 96            	ldw	x,sp
4021  014a 1c0009        	addw	x,#OFST-5
4022  014d cd0000        	call	_sprintf
4024  0150 5b06          	addw	sp,#6
4025                     ; 73 	for(i = 0; i< 10; i++) sendChar(output[i]);
4027  0152 5f            	clrw	x
4028  0153 1f0d          	ldw	(OFST-1,sp),x
4030  0155               L7652:
4033  0155 96            	ldw	x,sp
4034  0156 1c0003        	addw	x,#OFST-11
4035  0159 1f01          	ldw	(OFST-13,sp),x
4037  015b 1e0d          	ldw	x,(OFST-1,sp)
4038  015d 72fb01        	addw	x,(OFST-13,sp)
4039  0160 f6            	ld	a,(x)
4040  0161 cd0021        	call	_sendChar
4044  0164 1e0d          	ldw	x,(OFST-1,sp)
4045  0166 1c0001        	addw	x,#1
4046  0169 1f0d          	ldw	(OFST-1,sp),x
4050  016b 9c            	rvf
4051  016c 1e0d          	ldw	x,(OFST-1,sp)
4052  016e a3000a        	cpw	x,#10
4053  0171 2fe2          	jrslt	L7652
4054                     ; 74 }
4057  0173 5b0e          	addw	sp,#14
4058  0175 81            	ret
4071                     	xdef	_sendFloat
4072                     	xdef	_sendFloatAsString
4073                     	xdef	_sendLong
4074                     	xdef	_sendFloatAsLong
4075                     	xdef	_sendString
4076                     	xdef	_sendChar
4077                     	xdef	_sendDouble
4078                     	xdef	_UART_INIT
4079                     	xref	_sprintf
4080                     	xref	_strlen
4081                     .const:	section	.text
4082  0000               L5652:
4083  0000 256600        	dc.b	"%f",0
4084  0003               L1442:
4085  0003 49742400      	dc.w	18804,9216
4086                     	xref.b	c_lreg
4087                     	xref.b	c_x
4088                     	xref.b	c_y
4108                     	xref	c_lursh
4109                     	xref	c_rtol
4110                     	xref	c_ftol
4111                     	xref	c_fmul
4112                     	xref	c_ltor
4113                     	end
