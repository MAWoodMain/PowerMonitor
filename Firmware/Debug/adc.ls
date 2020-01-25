   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.11.14 - 18 Nov 2019
   3                     ; Generator (Limited) V4.4.11 - 19 Nov 2019
3316                     	bsct
3317  0000               _CHANNELS:
3318  0000 12            	dc.b	18
3319  0001 11            	dc.b	17
3320  0002 10            	dc.b	16
3321  0003 0f            	dc.b	15
3322  0004 0e            	dc.b	14
3323  0005 0d            	dc.b	13
3324  0006 0c            	dc.b	12
3325  0007 0b            	dc.b	11
3326  0008 04            	dc.b	4
3327  0009               _VOLTAGE_CHANNEL:
3328  0009 16            	dc.b	22
3361                     ; 6 void ADC_INIT()
3361                     ; 7 {
3363                     	switch	.text
3364  0000               _ADC_INIT:
3368                     ; 9 	CLK_PCKENR2 |= 0x01;
3370  0000 721050c4      	bset	_CLK_PCKENR2,#0
3371                     ; 16 	ADC1_CR1 = 0x01;
3373  0004 35015340      	mov	_ADC1_CR1,#1
3374                     ; 19 	ADC1_CR2 = 0x02;
3376  0008 35025341      	mov	_ADC1_CR2,#2
3377                     ; 21 	ADC1_CR3 = 0xE0;
3379  000c 35e05342      	mov	_ADC1_CR3,#224
3380                     ; 22 }
3383  0010 81            	ret
3425                     ; 24 int readChannel(int adcChannel)
3425                     ; 25 {
3426                     	switch	.text
3427  0011               _readChannel:
3429  0011 89            	pushw	x
3430       00000000      OFST:	set	0
3433                     ; 26 	ADC1_CR3 &= ~0x1F;
3435  0012 c65342        	ld	a,_ADC1_CR3
3436  0015 a4e0          	and	a,#224
3437  0017 c75342        	ld	_ADC1_CR3,a
3438                     ; 27 	ADC1_CR3 |= (0x1F & adcChannel);
3440  001a 9f            	ld	a,xl
3441  001b a41f          	and	a,#31
3442  001d ca5342        	or	a,_ADC1_CR3
3443  0020 c75342        	ld	_ADC1_CR3,a
3444                     ; 29 	ADC1_SQR2 = 0;
3446  0023 725f534b      	clr	_ADC1_SQR2
3447                     ; 30 	ADC1_SQR3 = 0;
3449  0027 725f534c      	clr	_ADC1_SQR3
3450                     ; 31 	ADC1_SQR4 = 0;
3452  002b 725f534d      	clr	_ADC1_SQR4
3453                     ; 33 	if (adcChannel > 15)
3455  002f 9c            	rvf
3456  0030 1e01          	ldw	x,(OFST+1,sp)
3457  0032 a30010        	cpw	x,#16
3458  0035 2f18          	jrslt	L7132
3459                     ; 35 		ADC1_SQR2 = (0x01 << (adcChannel-16));
3461  0037 7b02          	ld	a,(OFST+2,sp)
3462  0039 a010          	sub	a,#16
3463  003b 5f            	clrw	x
3464  003c 4d            	tnz	a
3465  003d 2a01          	jrpl	L01
3466  003f 53            	cplw	x
3467  0040               L01:
3468  0040 97            	ld	xl,a
3469  0041 a601          	ld	a,#1
3470  0043 5d            	tnzw	x
3471  0044 2704          	jreq	L21
3472  0046               L41:
3473  0046 48            	sll	a
3474  0047 5a            	decw	x
3475  0048 26fc          	jrne	L41
3476  004a               L21:
3477  004a c7534b        	ld	_ADC1_SQR2,a
3479  004d 2034          	jra	L1232
3480  004f               L7132:
3481                     ; 37 	else if (adcChannel > 7)
3483  004f 9c            	rvf
3484  0050 1e01          	ldw	x,(OFST+1,sp)
3485  0052 a30008        	cpw	x,#8
3486  0055 2f18          	jrslt	L3232
3487                     ; 39 		ADC1_SQR3 = (0x01 << (adcChannel-8));
3489  0057 7b02          	ld	a,(OFST+2,sp)
3490  0059 a008          	sub	a,#8
3491  005b 5f            	clrw	x
3492  005c 4d            	tnz	a
3493  005d 2a01          	jrpl	L61
3494  005f 53            	cplw	x
3495  0060               L61:
3496  0060 97            	ld	xl,a
3497  0061 a601          	ld	a,#1
3498  0063 5d            	tnzw	x
3499  0064 2704          	jreq	L02
3500  0066               L22:
3501  0066 48            	sll	a
3502  0067 5a            	decw	x
3503  0068 26fc          	jrne	L22
3504  006a               L02:
3505  006a c7534c        	ld	_ADC1_SQR3,a
3507  006d 2014          	jra	L1232
3508  006f               L3232:
3509                     ; 43 		ADC1_SQR4 = (0x01 << adcChannel);
3511  006f 7b02          	ld	a,(OFST+2,sp)
3512  0071 5f            	clrw	x
3513  0072 4d            	tnz	a
3514  0073 2a01          	jrpl	L42
3515  0075 53            	cplw	x
3516  0076               L42:
3517  0076 97            	ld	xl,a
3518  0077 a601          	ld	a,#1
3519  0079 5d            	tnzw	x
3520  007a 2704          	jreq	L62
3521  007c               L03:
3522  007c 48            	sll	a
3523  007d 5a            	decw	x
3524  007e 26fc          	jrne	L03
3525  0080               L62:
3526  0080 c7534d        	ld	_ADC1_SQR4,a
3527  0083               L1232:
3528                     ; 45 	ADC1_CR1 |= 0x02;
3530  0083 72125340      	bset	_ADC1_CR1,#1
3532  0087               L1332:
3533                     ; 47 	while(!(ADC1_SR & 0x01));
3535  0087 c65343        	ld	a,_ADC1_SR
3536  008a a501          	bcp	a,#1
3537  008c 27f9          	jreq	L1332
3538                     ; 48 	return (ADC1_DRH << 8)|ADC1_DRL;
3540  008e c65344        	ld	a,_ADC1_DRH
3541  0091 5f            	clrw	x
3542  0092 97            	ld	xl,a
3543  0093 c65345        	ld	a,_ADC1_DRL
3544  0096 02            	rlwa	x,a
3547  0097 5b02          	addw	sp,#2
3548  0099 81            	ret
3582                     	xdef	_readChannel
3583                     	xdef	_ADC_INIT
3584                     	xdef	_VOLTAGE_CHANNEL
3585                     	xdef	_CHANNELS
3604                     	end
