   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3351                     ; 3 void UART_INIT()
3351                     ; 4 {
3353                     	switch	.text
3354  0000               _UART_INIT:
3358                     ; 6 	CLK_PCKENR1 |= 0x20;
3360  0000 721a50c3      	bset	_CLK_PCKENR1,#5
3361                     ; 9 	PC_DDR |= 1 << 5;
3363  0004 721a500c      	bset	_PC_DDR,#5
3364                     ; 10 	PC_CR1 |= 1 << 5;
3366  0008 721a500d      	bset	_PC_CR1,#5
3367                     ; 13 	USART1_CR2 = USART_CR2_TEN;
3369  000c 35085235      	mov	_USART1_CR2,#8
3370                     ; 16 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3372  0010 c65236        	ld	a,_USART1_CR3
3373  0013 a4cf          	and	a,#207
3374  0015 c75236        	ld	_USART1_CR3,a
3375                     ; 19 	USART1_BRR2 = 0x05; 
3377  0018 35055233      	mov	_USART1_BRR2,#5
3378                     ; 20 	USART1_BRR1 = 0x04;
3380  001c 35045232      	mov	_USART1_BRR1,#4
3381                     ; 21 }
3384  0020 81            	ret
3420                     ; 23 void sendChar(unsigned char c)
3420                     ; 24 {
3421                     	switch	.text
3422  0021               _sendChar:
3424  0021 88            	push	a
3425       00000000      OFST:	set	0
3428  0022               L1232:
3429                     ; 25 	while(!(USART1_SR & USART_SR_TXE));
3431  0022 c65230        	ld	a,_USART1_SR
3432  0025 a580          	bcp	a,#128
3433  0027 27f9          	jreq	L1232
3434                     ; 26 	USART1_DR = c;
3436  0029 7b01          	ld	a,(OFST+1,sp)
3437  002b c75231        	ld	_USART1_DR,a
3438                     ; 27 }
3441  002e 84            	pop	a
3442  002f 81            	ret
3488                     ; 29 void sendString(const char *str) 
3488                     ; 30 {
3489                     	switch	.text
3490  0030               _sendString:
3492  0030 89            	pushw	x
3493  0031 88            	push	a
3494       00000001      OFST:	set	1
3497                     ; 32 	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
3499  0032 0f01          	clr	(OFST+0,sp)
3501  0034 2010          	jra	L3532
3502  0036               L7432:
3505  0036 7b02          	ld	a,(OFST+1,sp)
3506  0038 97            	ld	xl,a
3507  0039 7b03          	ld	a,(OFST+2,sp)
3508  003b 1b01          	add	a,(OFST+0,sp)
3509  003d 2401          	jrnc	L21
3510  003f 5c            	incw	x
3511  0040               L21:
3512  0040 02            	rlwa	x,a
3513  0041 f6            	ld	a,(x)
3514  0042 addd          	call	_sendChar
3518  0044 0c01          	inc	(OFST+0,sp)
3519  0046               L3532:
3522  0046 1e02          	ldw	x,(OFST+1,sp)
3523  0048 cd0000        	call	_strlen
3525  004b 7b01          	ld	a,(OFST+0,sp)
3526  004d 905f          	clrw	y
3527  004f 9097          	ld	yl,a
3528  0051 90bf00        	ldw	c_y,y
3529  0054 b300          	cpw	x,c_y
3530  0056 22de          	jrugt	L7432
3531                     ; 33 }
3534  0058 5b03          	addw	sp,#3
3535  005a 81            	ret
3590                     ; 35 void sendDouble(double double_value)
3590                     ; 36 {
3591                     	switch	.text
3592  005b               _sendDouble:
3594  005b 5209          	subw	sp,#9
3595       00000009      OFST:	set	9
3598                     ; 39 	memcpy(bytes, (unsigned char*) (&double_value), 8);
3600  005d 96            	ldw	x,sp
3601  005e 1c0001        	addw	x,#OFST-8
3602  0061 bf00          	ldw	c_x,x
3603  0063 9096          	ldw	y,sp
3604  0065 72a9000c      	addw	y,#OFST+3
3605  0069 90bf00        	ldw	c_y,y
3606  006c ae0008        	ldw	x,#8
3607  006f               L61:
3608  006f 5a            	decw	x
3609  0070 92d600        	ld	a,([c_y.w],x)
3610  0073 92d700        	ld	([c_x.w],x),a
3611  0076 5d            	tnzw	x
3612  0077 26f6          	jrne	L61
3613                     ; 40 	for(i = 0; i < 8; i++) sendChar(bytes[i]);
3615  0079 0f09          	clr	(OFST+0,sp)
3616  007b               L5042:
3619  007b 96            	ldw	x,sp
3620  007c 1c0001        	addw	x,#OFST-8
3621  007f 9f            	ld	a,xl
3622  0080 5e            	swapw	x
3623  0081 1b09          	add	a,(OFST+0,sp)
3624  0083 2401          	jrnc	L02
3625  0085 5c            	incw	x
3626  0086               L02:
3627  0086 02            	rlwa	x,a
3628  0087 f6            	ld	a,(x)
3629  0088 ad97          	call	_sendChar
3633  008a 0c09          	inc	(OFST+0,sp)
3636  008c 7b09          	ld	a,(OFST+0,sp)
3637  008e a108          	cp	a,#8
3638  0090 25e9          	jrult	L5042
3639                     ; 41 }
3642  0092 5b09          	addw	sp,#9
3643  0094 81            	ret
3687                     ; 43 void sendFloatAsLong(float double_value)
3687                     ; 44 {
3688                     	switch	.text
3689  0095               _sendFloatAsLong:
3691  0095 5204          	subw	sp,#4
3692       00000004      OFST:	set	4
3695                     ; 46 	longValue = (double_value*1000000.0);
3697  0097 96            	ldw	x,sp
3698  0098 1c0007        	addw	x,#OFST+3
3699  009b cd0000        	call	c_ltor
3701  009e ae0003        	ldw	x,#L1442
3702  00a1 cd0000        	call	c_fmul
3704  00a4 cd0000        	call	c_ftol
3706  00a7 96            	ldw	x,sp
3707  00a8 1c0001        	addw	x,#OFST-3
3708  00ab cd0000        	call	c_rtol
3710                     ; 47 	sendLong(longValue);
3712  00ae 1e03          	ldw	x,(OFST-1,sp)
3713  00b0 89            	pushw	x
3714  00b1 1e03          	ldw	x,(OFST-1,sp)
3715  00b3 89            	pushw	x
3716  00b4 ad05          	call	_sendLong
3718  00b6 5b04          	addw	sp,#4
3719                     ; 48 }
3722  00b8 5b04          	addw	sp,#4
3723  00ba 81            	ret
3767                     ; 50 void sendLong(unsigned long long_value)
3767                     ; 51 {
3768                     	switch	.text
3769  00bb               _sendLong:
3771  00bb 89            	pushw	x
3772       00000002      OFST:	set	2
3775                     ; 53 	for(i = 24; i >=0; i = i-8)
3777  00bc ae0018        	ldw	x,#24
3778  00bf 1f01          	ldw	(OFST-1,sp),x
3779  00c1               L7642:
3780                     ; 55 		sendChar((long_value >> i) & 0xFF);
3782  00c1 96            	ldw	x,sp
3783  00c2 1c0005        	addw	x,#OFST+3
3784  00c5 cd0000        	call	c_ltor
3786  00c8 7b02          	ld	a,(OFST+0,sp)
3787  00ca cd0000        	call	c_lursh
3789  00cd 3f02          	clr	c_lreg+2
3790  00cf 3f01          	clr	c_lreg+1
3791  00d1 3f00          	clr	c_lreg
3792  00d3 b603          	ld	a,c_lreg+3
3793  00d5 cd0021        	call	_sendChar
3795                     ; 53 	for(i = 24; i >=0; i = i-8)
3797  00d8 1e01          	ldw	x,(OFST-1,sp)
3798  00da 1d0008        	subw	x,#8
3799  00dd 1f01          	ldw	(OFST-1,sp),x
3802  00df 9c            	rvf
3803  00e0 1e01          	ldw	x,(OFST-1,sp)
3804  00e2 2edd          	jrsge	L7642
3805                     ; 57 }
3808  00e4 85            	popw	x
3809  00e5 81            	ret
3864                     ; 59 void sendFloat(float float_value)
3864                     ; 60 {
3865                     	switch	.text
3866  00e6               _sendFloat:
3868  00e6 5205          	subw	sp,#5
3869       00000005      OFST:	set	5
3872                     ; 63 	memcpy(bytes, (unsigned char*) (&float_value), 4);
3874  00e8 96            	ldw	x,sp
3875  00e9 1c0001        	addw	x,#OFST-4
3876  00ec bf00          	ldw	c_x,x
3877  00ee 9096          	ldw	y,sp
3878  00f0 72a90008      	addw	y,#OFST+3
3879  00f4 90bf00        	ldw	c_y,y
3880  00f7 ae0004        	ldw	x,#4
3881  00fa               L03:
3882  00fa 5a            	decw	x
3883  00fb 92d600        	ld	a,([c_y.w],x)
3884  00fe 92d700        	ld	([c_x.w],x),a
3885  0101 5d            	tnzw	x
3886  0102 26f6          	jrne	L03
3887                     ; 64 	for(i = 0; i < 4; i++) sendChar(bytes[i]);
3889  0104 0f05          	clr	(OFST+0,sp)
3890  0106               L3252:
3893  0106 96            	ldw	x,sp
3894  0107 1c0001        	addw	x,#OFST-4
3895  010a 9f            	ld	a,xl
3896  010b 5e            	swapw	x
3897  010c 1b05          	add	a,(OFST+0,sp)
3898  010e 2401          	jrnc	L23
3899  0110 5c            	incw	x
3900  0111               L23:
3901  0111 02            	rlwa	x,a
3902  0112 f6            	ld	a,(x)
3903  0113 cd0021        	call	_sendChar
3907  0116 0c05          	inc	(OFST+0,sp)
3910  0118 7b05          	ld	a,(OFST+0,sp)
3911  011a a104          	cp	a,#4
3912  011c 25e8          	jrult	L3252
3913                     ; 65 }
3916  011e 5b05          	addw	sp,#5
3917  0120 81            	ret
3972                     ; 67 void sendFloatAsString(float float_value)
3972                     ; 68 {
3973                     	switch	.text
3974  0121               _sendFloatAsString:
3976  0121 520e          	subw	sp,#14
3977       0000000e      OFST:	set	14
3980                     ; 71 	for(i = 0; i< 10; i++) output[i] = 0x30;
3982  0123 5f            	clrw	x
3983  0124 1f0d          	ldw	(OFST-1,sp),x
3984  0126               L7552:
3987  0126 96            	ldw	x,sp
3988  0127 1c0003        	addw	x,#OFST-11
3989  012a 1f01          	ldw	(OFST-13,sp),x
3990  012c 1e0d          	ldw	x,(OFST-1,sp)
3991  012e 72fb01        	addw	x,(OFST-13,sp)
3992  0131 a630          	ld	a,#48
3993  0133 f7            	ld	(x),a
3996  0134 1e0d          	ldw	x,(OFST-1,sp)
3997  0136 1c0001        	addw	x,#1
3998  0139 1f0d          	ldw	(OFST-1,sp),x
4001  013b 9c            	rvf
4002  013c 1e0d          	ldw	x,(OFST-1,sp)
4003  013e a3000a        	cpw	x,#10
4004  0141 2fe3          	jrslt	L7552
4005                     ; 72 	sprintf(output,"%f",float_value);
4007  0143 1e13          	ldw	x,(OFST+5,sp)
4008  0145 89            	pushw	x
4009  0146 1e13          	ldw	x,(OFST+5,sp)
4010  0148 89            	pushw	x
4011  0149 ae0000        	ldw	x,#L5652
4012  014c 89            	pushw	x
4013  014d 96            	ldw	x,sp
4014  014e 1c0009        	addw	x,#OFST-5
4015  0151 cd0000        	call	_sprintf
4017  0154 5b06          	addw	sp,#6
4018                     ; 73 	for(i = 0; i< 10; i++) sendChar(output[i]);
4020  0156 5f            	clrw	x
4021  0157 1f0d          	ldw	(OFST-1,sp),x
4022  0159               L7652:
4025  0159 96            	ldw	x,sp
4026  015a 1c0003        	addw	x,#OFST-11
4027  015d 1f01          	ldw	(OFST-13,sp),x
4028  015f 1e0d          	ldw	x,(OFST-1,sp)
4029  0161 72fb01        	addw	x,(OFST-13,sp)
4030  0164 f6            	ld	a,(x)
4031  0165 cd0021        	call	_sendChar
4035  0168 1e0d          	ldw	x,(OFST-1,sp)
4036  016a 1c0001        	addw	x,#1
4037  016d 1f0d          	ldw	(OFST-1,sp),x
4040  016f 9c            	rvf
4041  0170 1e0d          	ldw	x,(OFST-1,sp)
4042  0172 a3000a        	cpw	x,#10
4043  0175 2fe2          	jrslt	L7652
4044                     ; 74 }
4047  0177 5b0e          	addw	sp,#14
4048  0179 81            	ret
4061                     	xdef	_sendFloat
4062                     	xdef	_sendFloatAsString
4063                     	xdef	_sendLong
4064                     	xdef	_sendFloatAsLong
4065                     	xdef	_sendString
4066                     	xdef	_sendChar
4067                     	xdef	_sendDouble
4068                     	xdef	_UART_INIT
4069                     	xref	_sprintf
4070                     	xref	_strlen
4071                     .const:	section	.text
4072  0000               L5652:
4073  0000 256600        	dc.b	"%f",0
4074  0003               L1442:
4075  0003 49742400      	dc.w	18804,9216
4076                     	xref.b	c_lreg
4077                     	xref.b	c_x
4078                     	xref.b	c_y
4098                     	xref	c_lursh
4099                     	xref	c_rtol
4100                     	xref	c_ftol
4101                     	xref	c_fmul
4102                     	xref	c_ltor
4103                     	end
