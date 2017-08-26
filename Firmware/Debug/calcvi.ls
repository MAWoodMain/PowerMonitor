   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     .const:	section	.text
3318  0000               _ADC_COUNTS:
3319  0000 1000          	dc.w	4096
3320  0002               _PHASECAL:
3321  0002               L7622:
3322  0002 3fd99999      	dc.w	16345,-26215
3456                     ; 24 void calcVI(const char vPin, const char iPin, const unsigned int crossings)
3456                     ; 25 {
3458                     	switch	.text
3459  0000               _calcVI:
3461  0000 89            	pushw	x
3462  0001 5214          	subw	sp,#20
3463       00000014      OFST:	set	20
3466                     ; 26 	const float SupplyVoltage = 3.3;
3468  0003 ce001c        	ldw	x,L3632+2
3469  0006 1f0f          	ldw	(OFST-5,sp),x
3470  0008 ce001a        	ldw	x,L3632
3471  000b 1f0d          	ldw	(OFST-7,sp),x
3472                     ; 27   unsigned int crossCount = 0;
3474  000d 5f            	clrw	x
3475  000e 1f11          	ldw	(OFST-3,sp),x
3476                     ; 28   unsigned int numberOfSamples = 0;
3478  0010 5f            	clrw	x
3479  0011 1f13          	ldw	(OFST-1,sp),x
3480                     ; 31 	float VCAL = 210.0;
3482  0013 ce0018        	ldw	x,L3732+2
3483  0016 1f07          	ldw	(OFST-13,sp),x
3484  0018 ce0016        	ldw	x,L3732
3485  001b 1f05          	ldw	(OFST-15,sp),x
3486                     ; 34   sumV = 0;
3488  001d ae0000        	ldw	x,#0
3489  0020 bf14          	ldw	_sumV+2,x
3490  0022 ae0000        	ldw	x,#0
3491  0025 bf12          	ldw	_sumV,x
3492                     ; 35   sumI = 0;
3494  0027 ae0000        	ldw	x,#0
3495  002a bf10          	ldw	_sumI+2,x
3496  002c ae0000        	ldw	x,#0
3497  002f bf0e          	ldw	_sumI,x
3498                     ; 36   sumP = 0;	
3500  0031 ae0000        	ldw	x,#0
3501  0034 bf08          	ldw	_sumP+2,x
3502  0036 ae0000        	ldw	x,#0
3503  0039 bf06          	ldw	_sumP,x
3504  003b               L7732:
3505                     ; 39     startV = readChannel(vPin);
3507  003b 7b15          	ld	a,(OFST+1,sp)
3508  003d 5f            	clrw	x
3509  003e 97            	ld	xl,a
3510  003f cd0000        	call	_readChannel
3512  0042 bf04          	ldw	_startV,x
3513                     ; 41 	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
3515  0044 9c            	rvf
3516  0045 be04          	ldw	x,_startV
3517  0047 cd0000        	call	c_itof
3519  004a ae0012        	ldw	x,#L1142
3520  004d cd0000        	call	c_fcmp
3522  0050 2ee9          	jrsge	L7732
3524  0052 9c            	rvf
3525  0053 be04          	ldw	x,_startV
3526  0055 cd0000        	call	c_itof
3528  0058 ae000e        	ldw	x,#L1242
3529  005b cd0000        	call	c_fcmp
3531  005e 2ddb          	jrsle	L7732
3533  0060 ac790179      	jpf	L7242
3534  0064               L5242:
3535                     ; 46     numberOfSamples++;                       //Count number of times looped.
3537  0064 1e13          	ldw	x,(OFST-1,sp)
3538  0066 1c0001        	addw	x,#1
3539  0069 1f13          	ldw	(OFST-1,sp),x
3540                     ; 47     lastFilteredV = filteredV;               //Used for delay/phase compensation
3542  006b be28          	ldw	x,_filteredV+2
3543  006d bf2c          	ldw	_lastFilteredV+2,x
3544  006f be26          	ldw	x,_filteredV
3545  0071 bf2a          	ldw	_lastFilteredV,x
3546                     ; 52     sampleV = readChannel(vPin);                 //Read in raw voltage signal
3548  0073 7b15          	ld	a,(OFST+1,sp)
3549  0075 5f            	clrw	x
3550  0076 97            	ld	xl,a
3551  0077 cd0000        	call	_readChannel
3553  007a bf38          	ldw	_sampleV,x
3554                     ; 53     sampleI = readChannel(iPin);                 //Read in raw current signal
3556  007c 7b16          	ld	a,(OFST+2,sp)
3557  007e 5f            	clrw	x
3558  007f 97            	ld	xl,a
3559  0080 cd0000        	call	_readChannel
3561  0083 bf36          	ldw	_sampleI,x
3562                     ; 59     offsetV = offsetV + ((sampleV-offsetV)/1024);
3564  0085 be38          	ldw	x,_sampleV
3565  0087 cd0000        	call	c_itof
3567  008a ae001e        	ldw	x,#_offsetV
3568  008d cd0000        	call	c_fsub
3570  0090 ae000a        	ldw	x,#L7342
3571  0093 cd0000        	call	c_fdiv
3573  0096 ae001e        	ldw	x,#_offsetV
3574  0099 cd0000        	call	c_fgadd
3576                     ; 60     filteredV = sampleV - offsetV;
3578  009c be38          	ldw	x,_sampleV
3579  009e cd0000        	call	c_itof
3581  00a1 ae001e        	ldw	x,#_offsetV
3582  00a4 cd0000        	call	c_fsub
3584  00a7 ae0026        	ldw	x,#_filteredV
3585  00aa cd0000        	call	c_rtol
3587                     ; 61     offsetI = offsetI + ((sampleI-offsetI)/1024);
3589  00ad be36          	ldw	x,_sampleI
3590  00af cd0000        	call	c_itof
3592  00b2 ae001a        	ldw	x,#_offsetI
3593  00b5 cd0000        	call	c_fsub
3595  00b8 ae000a        	ldw	x,#L7342
3596  00bb cd0000        	call	c_fdiv
3598  00be ae001a        	ldw	x,#_offsetI
3599  00c1 cd0000        	call	c_fgadd
3601                     ; 62     filteredI = sampleI - offsetI;
3603  00c4 be36          	ldw	x,_sampleI
3604  00c6 cd0000        	call	c_itof
3606  00c9 ae001a        	ldw	x,#_offsetI
3607  00cc cd0000        	call	c_fsub
3609  00cf ae0022        	ldw	x,#_filteredI
3610  00d2 cd0000        	call	c_rtol
3612                     ; 68     sumV += filteredV * filteredV;
3614  00d5 ae0026        	ldw	x,#_filteredV
3615  00d8 cd0000        	call	c_ltor
3617  00db ae0026        	ldw	x,#_filteredV
3618  00de cd0000        	call	c_fmul
3620  00e1 ae0012        	ldw	x,#_sumV
3621  00e4 cd0000        	call	c_fgadd
3623                     ; 74     sumI += filteredI * filteredI;
3625  00e7 ae0022        	ldw	x,#_filteredI
3626  00ea cd0000        	call	c_ltor
3628  00ed ae0022        	ldw	x,#_filteredI
3629  00f0 cd0000        	call	c_fmul
3631  00f3 ae000e        	ldw	x,#_sumI
3632  00f6 cd0000        	call	c_fgadd
3634                     ; 79     phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
3636  00f9 ae0026        	ldw	x,#_filteredV
3637  00fc cd0000        	call	c_ltor
3639  00ff ae002a        	ldw	x,#_lastFilteredV
3640  0102 cd0000        	call	c_fsub
3642  0105 ae0002        	ldw	x,#L7622
3643  0108 cd0000        	call	c_fmul
3645  010b ae002a        	ldw	x,#_lastFilteredV
3646  010e cd0000        	call	c_fadd
3648  0111 ae0016        	ldw	x,#_phaseShiftedV
3649  0114 cd0000        	call	c_rtol
3651                     ; 84     instP = phaseShiftedV * filteredI;          //Instantaneous Power
3653  0117 ae0016        	ldw	x,#_phaseShiftedV
3654  011a cd0000        	call	c_ltor
3656  011d ae0022        	ldw	x,#_filteredI
3657  0120 cd0000        	call	c_fmul
3659  0123 ae000a        	ldw	x,#_instP
3660  0126 cd0000        	call	c_rtol
3662                     ; 85     sumP +=instP;                               //Sum
3664  0129 ae000a        	ldw	x,#_instP
3665  012c cd0000        	call	c_ltor
3667  012f ae0006        	ldw	x,#_sumP
3668  0132 cd0000        	call	c_fgadd
3670                     ; 92     lastVCross = checkVCross;
3672                     	btst		_checkVCross
3673  013a 90110001      	bccm	_lastVCross
3674                     ; 94 		checkVCross = sampleV > startV;
3676  013e 9c            	rvf
3677  013f be38          	ldw	x,_sampleV
3678  0141 b304          	cpw	x,_startV
3679  0143 2c02          	jrsgt	L61
3680  0145 2006          	jp	L6
3681  0147               L61:
3682  0147 72100000      	bset	_checkVCross
3683  014b 2004          	jra	L01
3684  014d               L6:
3685  014d 72110000      	bres	_checkVCross
3686  0151               L01:
3687                     ; 99     if (numberOfSamples==1) lastVCross = checkVCross;
3689  0151 1e13          	ldw	x,(OFST-1,sp)
3690  0153 a30001        	cpw	x,#1
3691  0156 2609          	jrne	L3442
3694                     	btst		_checkVCross
3695  015d 90110001      	bccm	_lastVCross
3696  0161               L3442:
3697                     ; 101     if (lastVCross != checkVCross) crossCount++;
3699  0161 7201000107    	btjf	_lastVCross,L21
3700  0166 720000000e    	btjt	_checkVCross,L7242
3701  016b 2005          	jra	L41
3702  016d               L21:
3703  016d 7201000007    	btjf	_checkVCross,L7242
3704  0172               L41:
3707  0172 1e11          	ldw	x,(OFST-3,sp)
3708  0174 1c0001        	addw	x,#1
3709  0177 1f11          	ldw	(OFST-3,sp),x
3710  0179               L7242:
3711                     ; 43 	while(crossCount < crossings)
3713  0179 1e11          	ldw	x,(OFST-3,sp)
3714  017b 1319          	cpw	x,(OFST+5,sp)
3715  017d 2403          	jruge	L02
3716  017f cc0064        	jp	L5242
3717  0182               L02:
3718                     ; 110   V_RATIO = SupplyVoltage / ADC_COUNTS;//VCAL *(SupplyVoltage / ADC_COUNTS);
3720  0182 96            	ldw	x,sp
3721  0183 1c000d        	addw	x,#OFST-7
3722  0186 cd0000        	call	c_ltor
3724  0189 ae0006        	ldw	x,#L3542
3725  018c cd0000        	call	c_fdiv
3727  018f 96            	ldw	x,sp
3728  0190 1c0009        	addw	x,#OFST-11
3729  0193 cd0000        	call	c_rtol
3731                     ; 111   Vrms = V_RATIO * sqrt(sumV / numberOfSamples);
3733  0196 1e13          	ldw	x,(OFST-1,sp)
3734  0198 cd0000        	call	c_uitof
3736  019b 96            	ldw	x,sp
3737  019c 1c0001        	addw	x,#OFST-19
3738  019f cd0000        	call	c_rtol
3740  01a2 ae0012        	ldw	x,#_sumV
3741  01a5 cd0000        	call	c_ltor
3743  01a8 96            	ldw	x,sp
3744  01a9 1c0001        	addw	x,#OFST-19
3745  01ac cd0000        	call	c_fdiv
3747  01af be02          	ldw	x,c_lreg+2
3748  01b1 89            	pushw	x
3749  01b2 be00          	ldw	x,c_lreg
3750  01b4 89            	pushw	x
3751  01b5 cd0000        	call	_sqrt
3753  01b8 5b04          	addw	sp,#4
3754  01ba 96            	ldw	x,sp
3755  01bb 1c0009        	addw	x,#OFST-11
3756  01be cd0000        	call	c_fmul
3758  01c1 ae003e        	ldw	x,#_Vrms
3759  01c4 cd0000        	call	c_rtol
3761                     ; 113   I_RATIO = SupplyVoltage / ADC_COUNTS;//ICAL *(SupplyVoltage / ADC_COUNTS);
3763  01c7 96            	ldw	x,sp
3764  01c8 1c000d        	addw	x,#OFST-7
3765  01cb cd0000        	call	c_ltor
3767  01ce ae0006        	ldw	x,#L3542
3768  01d1 cd0000        	call	c_fdiv
3770  01d4 96            	ldw	x,sp
3771  01d5 1c000d        	addw	x,#OFST-7
3772  01d8 cd0000        	call	c_rtol
3774                     ; 114   Irms = I_RATIO * sqrt(sumI / numberOfSamples);
3776  01db 1e13          	ldw	x,(OFST-1,sp)
3777  01dd cd0000        	call	c_uitof
3779  01e0 96            	ldw	x,sp
3780  01e1 1c0001        	addw	x,#OFST-19
3781  01e4 cd0000        	call	c_rtol
3783  01e7 ae000e        	ldw	x,#_sumI
3784  01ea cd0000        	call	c_ltor
3786  01ed 96            	ldw	x,sp
3787  01ee 1c0001        	addw	x,#OFST-19
3788  01f1 cd0000        	call	c_fdiv
3790  01f4 be02          	ldw	x,c_lreg+2
3791  01f6 89            	pushw	x
3792  01f7 be00          	ldw	x,c_lreg
3793  01f9 89            	pushw	x
3794  01fa cd0000        	call	_sqrt
3796  01fd 5b04          	addw	sp,#4
3797  01ff 96            	ldw	x,sp
3798  0200 1c000d        	addw	x,#OFST-7
3799  0203 cd0000        	call	c_fmul
3801  0206 ae003a        	ldw	x,#_Irms
3802  0209 cd0000        	call	c_rtol
3804                     ; 117   realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
3806  020c 1e13          	ldw	x,(OFST-1,sp)
3807  020e cd0000        	call	c_uitof
3809  0211 96            	ldw	x,sp
3810  0212 1c0001        	addw	x,#OFST-19
3811  0215 cd0000        	call	c_rtol
3813  0218 96            	ldw	x,sp
3814  0219 1c0009        	addw	x,#OFST-11
3815  021c cd0000        	call	c_ltor
3817  021f 96            	ldw	x,sp
3818  0220 1c000d        	addw	x,#OFST-7
3819  0223 cd0000        	call	c_fmul
3821  0226 ae0006        	ldw	x,#_sumP
3822  0229 cd0000        	call	c_fmul
3824  022c 96            	ldw	x,sp
3825  022d 1c0001        	addw	x,#OFST-19
3826  0230 cd0000        	call	c_fdiv
3828  0233 ae0046        	ldw	x,#_realPower
3829  0236 cd0000        	call	c_rtol
3831                     ; 120 }
3834  0239 5b16          	addw	sp,#22
3835  023b 81            	ret
3859                     ; 122 float getRealPower()
3859                     ; 123 {
3860                     	switch	.text
3861  023c               _getRealPower:
3865                     ; 124 	return realPower;
3867  023c ae0046        	ldw	x,#_realPower
3868  023f cd0000        	call	c_ltor
3872  0242 81            	ret
3896                     ; 127 float getIrms()
3896                     ; 128 {
3897                     	switch	.text
3898  0243               _getIrms:
3902                     ; 129 	return Irms;
3904  0243 ae003a        	ldw	x,#_Irms
3905  0246 cd0000        	call	c_ltor
3909  0249 81            	ret
3933                     ; 132 float getVrms()
3933                     ; 133 {
3934                     	switch	.text
3935  024a               _getVrms:
3939                     ; 134 	return Vrms;
3941  024a ae003e        	ldw	x,#_Vrms
3942  024d cd0000        	call	c_ltor
3946  0250 81            	ret
4179                     	switch	.ubsct
4180  0000               _apparentPower:
4181  0000 00000000      	ds.b	4
4182                     	xdef	_apparentPower
4183                     .bit:	section	.data,bit
4184  0000               _checkVCross:
4185  0000 00            	ds.b	1
4186                     	xdef	_checkVCross
4187  0001               _lastVCross:
4188  0001 00            	ds.b	1
4189                     	xdef	_lastVCross
4190                     	switch	.ubsct
4191  0004               _startV:
4192  0004 0000          	ds.b	2
4193                     	xdef	_startV
4194  0006               _sumP:
4195  0006 00000000      	ds.b	4
4196                     	xdef	_sumP
4197  000a               _instP:
4198  000a 00000000      	ds.b	4
4199                     	xdef	_instP
4200  000e               _sumI:
4201  000e 00000000      	ds.b	4
4202                     	xdef	_sumI
4203  0012               _sumV:
4204  0012 00000000      	ds.b	4
4205                     	xdef	_sumV
4206  0016               _phaseShiftedV:
4207  0016 00000000      	ds.b	4
4208                     	xdef	_phaseShiftedV
4209  001a               _offsetI:
4210  001a 00000000      	ds.b	4
4211                     	xdef	_offsetI
4212  001e               _offsetV:
4213  001e 00000000      	ds.b	4
4214                     	xdef	_offsetV
4215  0022               _filteredI:
4216  0022 00000000      	ds.b	4
4217                     	xdef	_filteredI
4218  0026               _filteredV:
4219  0026 00000000      	ds.b	4
4220                     	xdef	_filteredV
4221  002a               _lastFilteredV:
4222  002a 00000000      	ds.b	4
4223                     	xdef	_lastFilteredV
4224  002e               _ICAL:
4225  002e 00000000      	ds.b	4
4226                     	xdef	_ICAL
4227  0032               _VCAL:
4228  0032 00000000      	ds.b	4
4229                     	xdef	_VCAL
4230  0036               _sampleI:
4231  0036 0000          	ds.b	2
4232                     	xdef	_sampleI
4233  0038               _sampleV:
4234  0038 0000          	ds.b	2
4235                     	xdef	_sampleV
4236  003a               _Irms:
4237  003a 00000000      	ds.b	4
4238                     	xdef	_Irms
4239  003e               _Vrms:
4240  003e 00000000      	ds.b	4
4241                     	xdef	_Vrms
4242  0042               _powerFactor:
4243  0042 00000000      	ds.b	4
4244                     	xdef	_powerFactor
4245  0046               _realPower:
4246  0046 00000000      	ds.b	4
4247                     	xdef	_realPower
4248                     	xdef	_getRealPower
4249                     	xdef	_getIrms
4250                     	xdef	_getVrms
4251                     	xdef	_calcVI
4252                     	xdef	_PHASECAL
4253                     	xdef	_ADC_COUNTS
4254                     	xref	_readChannel
4255                     	xref	_sqrt
4256                     	switch	.const
4257  0006               L3542:
4258  0006 45800000      	dc.w	17792,0
4259  000a               L7342:
4260  000a 44800000      	dc.w	17536,0
4261  000e               L1242:
4262  000e 44e66666      	dc.w	17638,26214
4263  0012               L1142:
4264  0012 450ccccc      	dc.w	17676,-13108
4265  0016               L3732:
4266  0016 43520000      	dc.w	17234,0
4267  001a               L3632:
4268  001a 40533333      	dc.w	16467,13107
4269                     	xref.b	c_lreg
4270                     	xref.b	c_x
4290                     	xref	c_uitof
4291                     	xref	c_fadd
4292                     	xref	c_fmul
4293                     	xref	c_ltor
4294                     	xref	c_rtol
4295                     	xref	c_fgadd
4296                     	xref	c_fdiv
4297                     	xref	c_fsub
4298                     	xref	c_fcmp
4299                     	xref	c_itof
4300                     	end
