   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.11.14 - 18 Nov 2019
   3                     ; Generator (Limited) V4.4.11 - 19 Nov 2019
3354                     ; 3 void RTC_INIT()
3354                     ; 4 {
3356                     	switch	.text
3357  0000               _RTC_INIT:
3361                     ; 6 	CLK_PCKENR2 |= 0x04;
3363  0000 721450c4      	bset	_CLK_PCKENR2,#2
3364                     ; 8 	CLK_CRTCR |= 0x02;
3366  0004 721250c1      	bset	_CLK_CRTCR,#1
3367                     ; 11 	RTC_WPR = 0xCA;
3369  0008 35ca5159      	mov	_RTC_WPR,#202
3370                     ; 12 	RTC_WPR = 0x53;
3372  000c 35535159      	mov	_RTC_WPR,#83
3373                     ; 15 	if ((RTC_ISR1 & 0x40) == 0)
3375  0010 c6514c        	ld	a,_RTC_ISR1
3376  0013 a540          	bcp	a,#64
3377  0015 260b          	jrne	L1032
3378                     ; 18     RTC_ISR1 = 0x80;
3380  0017 3580514c      	mov	_RTC_ISR1,#128
3382  001b               L7032:
3383                     ; 21     while ((RTC_ISR1 & 0x40) == 0);
3385  001b c6514c        	ld	a,_RTC_ISR1
3386  001e a540          	bcp	a,#64
3387  0020 27f9          	jreq	L7032
3388  0022               L1032:
3389                     ; 24 	RTC_TR1 = 0x02;
3391  0022 35025140      	mov	_RTC_TR1,#2
3392                     ; 25 	RTC_TR2 = 0x35;
3394  0026 35355141      	mov	_RTC_TR2,#53
3395                     ; 26 	RTC_TR3 = 0x53;
3397  002a 35535142      	mov	_RTC_TR3,#83
3398                     ; 28 	RTC_DR1 = 0x20;
3400  002e 35205144      	mov	_RTC_DR1,#32
3401                     ; 29 	RTC_DR2 = 0xC8;
3403  0032 35c85145      	mov	_RTC_DR2,#200
3404                     ; 30 	RTC_DR3 = 0x17;
3406  0036 35175146      	mov	_RTC_DR3,#23
3407                     ; 32 	RTC_ISR1 =0x00;
3409  003a 725f514c      	clr	_RTC_ISR1
3410                     ; 33 	RTC_ISR2 =0x00;
3412  003e 725f514d      	clr	_RTC_ISR2
3413                     ; 34 	RTC_WPR = 0xFF; 
3415  0042 35ff5159      	mov	_RTC_WPR,#255
3416                     ; 35 }
3419  0046 81            	ret
3432                     	xdef	_RTC_INIT
3451                     	end
