   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3355                     ; 3 void RTC_INIT()
3355                     ; 4 {
3357                     	switch	.text
3358  0000               _RTC_INIT:
3362                     ; 6 	CLK_PCKENR2 |= 0x04;
3364  0000 721450c4      	bset	_CLK_PCKENR2,#2
3365                     ; 8 	CLK_CRTCR |= 0x02;
3367  0004 721250c1      	bset	_CLK_CRTCR,#1
3368                     ; 11 	RTC_WPR = 0xCA;
3370  0008 35ca5159      	mov	_RTC_WPR,#202
3371                     ; 12 	RTC_WPR = 0x53;
3373  000c 35535159      	mov	_RTC_WPR,#83
3374                     ; 15 	if ((RTC_ISR1 & 0x40) == 0)
3376  0010 c6514c        	ld	a,_RTC_ISR1
3377  0013 a540          	bcp	a,#64
3378  0015 260b          	jrne	L1032
3379                     ; 18     RTC_ISR1 = 0x80;
3381  0017 3580514c      	mov	_RTC_ISR1,#128
3383  001b               L7032:
3384                     ; 21     while ((RTC_ISR1 & 0x40) == 0);
3386  001b c6514c        	ld	a,_RTC_ISR1
3387  001e a540          	bcp	a,#64
3388  0020 27f9          	jreq	L7032
3389  0022               L1032:
3390                     ; 24 	RTC_TR1 = 0x02;
3392  0022 35025140      	mov	_RTC_TR1,#2
3393                     ; 25 	RTC_TR2 = 0x35;
3395  0026 35355141      	mov	_RTC_TR2,#53
3396                     ; 26 	RTC_TR3 = 0x53;
3398  002a 35535142      	mov	_RTC_TR3,#83
3399                     ; 28 	RTC_DR1 = 0x20;
3401  002e 35205144      	mov	_RTC_DR1,#32
3402                     ; 29 	RTC_DR2 = 0xC8;
3404  0032 35c85145      	mov	_RTC_DR2,#200
3405                     ; 30 	RTC_DR3 = 0x17;
3407  0036 35175146      	mov	_RTC_DR3,#23
3408                     ; 32 	RTC_ISR1 =0x00;
3410  003a 725f514c      	clr	_RTC_ISR1
3411                     ; 33 	RTC_ISR2 =0x00;
3413  003e 725f514d      	clr	_RTC_ISR2
3414                     ; 34 	RTC_WPR = 0xFF; 
3416  0042 35ff5159      	mov	_RTC_WPR,#255
3417                     ; 35 }
3420  0046 81            	ret
3433                     	xdef	_RTC_INIT
3452                     	end
