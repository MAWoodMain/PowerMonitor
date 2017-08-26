   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     .const:	section	.text
3318  0000               _ADC_COUNTS:
3319  0000 1000          	dc.w	4096
3320  0002               _PHASECAL:
3321  0002               L7622:
3322  0002 3fd99999      	dc.w	16345,-26215
3466                     ; 24 void calcVI(const char vPin, const char iPin, const unsigned int crossings)
3466                     ; 25 {
3468                     	switch	.text
3469  0000               _calcVI:
3471  0000 89            	pushw	x
3472  0001 5214          	subw	sp,#20
3473       00000014      OFST:	set	20
3476                     ; 26 	const float SupplyVoltage = 3.3;
3478  0003 ce001c        	ldw	x,L7632+2
3479  0006 1f0f          	ldw	(OFST-5,sp),x
3480  0008 ce001a        	ldw	x,L7632
3481  000b 1f0d          	ldw	(OFST-7,sp),x
3482                     ; 27   unsigned int crossCount = 0;
3484  000d 5f            	clrw	x
3485  000e 1f11          	ldw	(OFST-3,sp),x
3486                     ; 28   unsigned int numberOfSamples = 0;
3488  0010 5f            	clrw	x
3489  0011 1f13          	ldw	(OFST-1,sp),x
3490                     ; 31 	float VCAL = 210.0;
3492  0013 ce0018        	ldw	x,L7732+2
3493  0016 1f0b          	ldw	(OFST-9,sp),x
3494  0018 ce0016        	ldw	x,L7732
3495  001b 1f09          	ldw	(OFST-11,sp),x
3496                     ; 32 	float ICAL = 1800/372; // 1800 turns / burden resistor valuer for 5A clamp 372 Ohms ~4.8387
3498  001d a604          	ld	a,#4
3499  001f cd0000        	call	c_ctof
3501  0022 96            	ldw	x,sp
3502  0023 1c0005        	addw	x,#OFST-15
3503  0026 cd0000        	call	c_rtol
3505                     ; 35   sumV = 0;
3507  0029 ae0000        	ldw	x,#0
3508  002c bf10          	ldw	_sumV+2,x
3509  002e ae0000        	ldw	x,#0
3510  0031 bf0e          	ldw	_sumV,x
3511                     ; 36   sumI = 0;
3513  0033 ae0000        	ldw	x,#0
3514  0036 bf0c          	ldw	_sumI+2,x
3515  0038 ae0000        	ldw	x,#0
3516  003b bf0a          	ldw	_sumI,x
3517                     ; 37   sumP = 0;	
3519  003d ae0000        	ldw	x,#0
3520  0040 bf04          	ldw	_sumP+2,x
3521  0042 ae0000        	ldw	x,#0
3522  0045 bf02          	ldw	_sumP,x
3523  0047               L3042:
3524                     ; 40     startV = readChannel(vPin);
3526  0047 7b15          	ld	a,(OFST+1,sp)
3527  0049 5f            	clrw	x
3528  004a 97            	ld	xl,a
3529  004b cd0000        	call	_readChannel
3531  004e bf00          	ldw	_startV,x
3532                     ; 42 	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
3534  0050 9c            	rvf
3535  0051 be00          	ldw	x,_startV
3536  0053 cd0000        	call	c_itof
3538  0056 ae0012        	ldw	x,#L5142
3539  0059 cd0000        	call	c_fcmp
3541  005c 2ee9          	jrsge	L3042
3543  005e 9c            	rvf
3544  005f be00          	ldw	x,_startV
3545  0061 cd0000        	call	c_itof
3547  0064 ae000e        	ldw	x,#L5242
3548  0067 cd0000        	call	c_fcmp
3550  006a 2ddb          	jrsle	L3042
3552  006c ac850185      	jpf	L3342
3553  0070               L1342:
3554                     ; 47     numberOfSamples++;                       //Count number of times looped.
3556  0070 1e13          	ldw	x,(OFST-1,sp)
3557  0072 1c0001        	addw	x,#1
3558  0075 1f13          	ldw	(OFST-1,sp),x
3559                     ; 48     lastFilteredV = filteredV;               //Used for delay/phase compensation
3561  0077 be24          	ldw	x,_filteredV+2
3562  0079 bf28          	ldw	_lastFilteredV+2,x
3563  007b be22          	ldw	x,_filteredV
3564  007d bf26          	ldw	_lastFilteredV,x
3565                     ; 53     sampleV = readChannel(vPin);                 //Read in raw voltage signal
3567  007f 7b15          	ld	a,(OFST+1,sp)
3568  0081 5f            	clrw	x
3569  0082 97            	ld	xl,a
3570  0083 cd0000        	call	_readChannel
3572  0086 bf34          	ldw	_sampleV,x
3573                     ; 54     sampleI = readChannel(iPin);                 //Read in raw current signal
3575  0088 7b16          	ld	a,(OFST+2,sp)
3576  008a 5f            	clrw	x
3577  008b 97            	ld	xl,a
3578  008c cd0000        	call	_readChannel
3580  008f bf32          	ldw	_sampleI,x
3581                     ; 60     offsetV = offsetV + ((sampleV-offsetV)/1024);
3583  0091 be34          	ldw	x,_sampleV
3584  0093 cd0000        	call	c_itof
3586  0096 ae001a        	ldw	x,#_offsetV
3587  0099 cd0000        	call	c_fsub
3589  009c ae000a        	ldw	x,#L3442
3590  009f cd0000        	call	c_fdiv
3592  00a2 ae001a        	ldw	x,#_offsetV
3593  00a5 cd0000        	call	c_fgadd
3595                     ; 61     filteredV = sampleV - offsetV;
3597  00a8 be34          	ldw	x,_sampleV
3598  00aa cd0000        	call	c_itof
3600  00ad ae001a        	ldw	x,#_offsetV
3601  00b0 cd0000        	call	c_fsub
3603  00b3 ae0022        	ldw	x,#_filteredV
3604  00b6 cd0000        	call	c_rtol
3606                     ; 62     offsetI = offsetI + ((sampleI-offsetI)/1024);
3608  00b9 be32          	ldw	x,_sampleI
3609  00bb cd0000        	call	c_itof
3611  00be ae0016        	ldw	x,#_offsetI
3612  00c1 cd0000        	call	c_fsub
3614  00c4 ae000a        	ldw	x,#L3442
3615  00c7 cd0000        	call	c_fdiv
3617  00ca ae0016        	ldw	x,#_offsetI
3618  00cd cd0000        	call	c_fgadd
3620                     ; 63     filteredI = sampleI - offsetI;
3622  00d0 be32          	ldw	x,_sampleI
3623  00d2 cd0000        	call	c_itof
3625  00d5 ae0016        	ldw	x,#_offsetI
3626  00d8 cd0000        	call	c_fsub
3628  00db ae001e        	ldw	x,#_filteredI
3629  00de cd0000        	call	c_rtol
3631                     ; 69     sumV += filteredV * filteredV;
3633  00e1 ae0022        	ldw	x,#_filteredV
3634  00e4 cd0000        	call	c_ltor
3636  00e7 ae0022        	ldw	x,#_filteredV
3637  00ea cd0000        	call	c_fmul
3639  00ed ae000e        	ldw	x,#_sumV
3640  00f0 cd0000        	call	c_fgadd
3642                     ; 75     sumI += filteredI * filteredI;
3644  00f3 ae001e        	ldw	x,#_filteredI
3645  00f6 cd0000        	call	c_ltor
3647  00f9 ae001e        	ldw	x,#_filteredI
3648  00fc cd0000        	call	c_fmul
3650  00ff ae000a        	ldw	x,#_sumI
3651  0102 cd0000        	call	c_fgadd
3653                     ; 80     phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
3655  0105 ae0022        	ldw	x,#_filteredV
3656  0108 cd0000        	call	c_ltor
3658  010b ae0026        	ldw	x,#_lastFilteredV
3659  010e cd0000        	call	c_fsub
3661  0111 ae0002        	ldw	x,#L7622
3662  0114 cd0000        	call	c_fmul
3664  0117 ae0026        	ldw	x,#_lastFilteredV
3665  011a cd0000        	call	c_fadd
3667  011d ae0012        	ldw	x,#_phaseShiftedV
3668  0120 cd0000        	call	c_rtol
3670                     ; 85     instP = phaseShiftedV * filteredI;          //Instantaneous Power
3672  0123 ae0012        	ldw	x,#_phaseShiftedV
3673  0126 cd0000        	call	c_ltor
3675  0129 ae001e        	ldw	x,#_filteredI
3676  012c cd0000        	call	c_fmul
3678  012f ae0006        	ldw	x,#_instP
3679  0132 cd0000        	call	c_rtol
3681                     ; 86     sumP +=instP;                               //Sum
3683  0135 ae0006        	ldw	x,#_instP
3684  0138 cd0000        	call	c_ltor
3686  013b ae0002        	ldw	x,#_sumP
3687  013e cd0000        	call	c_fgadd
3689                     ; 93     lastVCross = checkVCross;
3691                     	btst		_checkVCross
3692  0146 90110001      	bccm	_lastVCross
3693                     ; 95 		checkVCross = sampleV > startV;
3695  014a 9c            	rvf
3696  014b be34          	ldw	x,_sampleV
3697  014d b300          	cpw	x,_startV
3698  014f 2c02          	jrsgt	L61
3699  0151 2006          	jp	L6
3700  0153               L61:
3701  0153 72100000      	bset	_checkVCross
3702  0157 2004          	jra	L01
3703  0159               L6:
3704  0159 72110000      	bres	_checkVCross
3705  015d               L01:
3706                     ; 100     if (numberOfSamples==1) lastVCross = checkVCross;
3708  015d 1e13          	ldw	x,(OFST-1,sp)
3709  015f a30001        	cpw	x,#1
3710  0162 2609          	jrne	L7442
3713                     	btst		_checkVCross
3714  0169 90110001      	bccm	_lastVCross
3715  016d               L7442:
3716                     ; 102     if (lastVCross != checkVCross) crossCount++;
3718  016d 7201000107    	btjf	_lastVCross,L21
3719  0172 720000000e    	btjt	_checkVCross,L3342
3720  0177 2005          	jra	L41
3721  0179               L21:
3722  0179 7201000007    	btjf	_checkVCross,L3342
3723  017e               L41:
3726  017e 1e11          	ldw	x,(OFST-3,sp)
3727  0180 1c0001        	addw	x,#1
3728  0183 1f11          	ldw	(OFST-3,sp),x
3729  0185               L3342:
3730                     ; 44 	while(crossCount < crossings)
3732  0185 1e11          	ldw	x,(OFST-3,sp)
3733  0187 1319          	cpw	x,(OFST+5,sp)
3734  0189 2403          	jruge	L02
3735  018b cc0070        	jp	L1342
3736  018e               L02:
3737                     ; 111   V_RATIO = VCAL *(SupplyVoltage / ADC_COUNTS);
3739  018e 96            	ldw	x,sp
3740  018f 1c000d        	addw	x,#OFST-7
3741  0192 cd0000        	call	c_ltor
3743  0195 ae0006        	ldw	x,#L7542
3744  0198 cd0000        	call	c_fdiv
3746  019b 96            	ldw	x,sp
3747  019c 1c0009        	addw	x,#OFST-11
3748  019f cd0000        	call	c_fmul
3750  01a2 96            	ldw	x,sp
3751  01a3 1c0009        	addw	x,#OFST-11
3752  01a6 cd0000        	call	c_rtol
3754                     ; 112   Vrms = V_RATIO * sqrt(sumV / numberOfSamples);
3756  01a9 1e13          	ldw	x,(OFST-1,sp)
3757  01ab cd0000        	call	c_uitof
3759  01ae 96            	ldw	x,sp
3760  01af 1c0001        	addw	x,#OFST-19
3761  01b2 cd0000        	call	c_rtol
3763  01b5 ae000e        	ldw	x,#_sumV
3764  01b8 cd0000        	call	c_ltor
3766  01bb 96            	ldw	x,sp
3767  01bc 1c0001        	addw	x,#OFST-19
3768  01bf cd0000        	call	c_fdiv
3770  01c2 be02          	ldw	x,c_lreg+2
3771  01c4 89            	pushw	x
3772  01c5 be00          	ldw	x,c_lreg
3773  01c7 89            	pushw	x
3774  01c8 cd0000        	call	_sqrt
3776  01cb 5b04          	addw	sp,#4
3777  01cd 96            	ldw	x,sp
3778  01ce 1c0009        	addw	x,#OFST-11
3779  01d1 cd0000        	call	c_fmul
3781  01d4 ae003a        	ldw	x,#_Vrms
3782  01d7 cd0000        	call	c_rtol
3784                     ; 114   I_RATIO = SupplyVoltage / ADC_COUNTS;//ICAL *(SupplyVoltage / ADC_COUNTS);
3786  01da 96            	ldw	x,sp
3787  01db 1c000d        	addw	x,#OFST-7
3788  01de cd0000        	call	c_ltor
3790  01e1 ae0006        	ldw	x,#L7542
3791  01e4 cd0000        	call	c_fdiv
3793  01e7 96            	ldw	x,sp
3794  01e8 1c000d        	addw	x,#OFST-7
3795  01eb cd0000        	call	c_rtol
3797                     ; 115   Irms = I_RATIO * sqrt(sumI / numberOfSamples);
3799  01ee 1e13          	ldw	x,(OFST-1,sp)
3800  01f0 cd0000        	call	c_uitof
3802  01f3 96            	ldw	x,sp
3803  01f4 1c0001        	addw	x,#OFST-19
3804  01f7 cd0000        	call	c_rtol
3806  01fa ae000a        	ldw	x,#_sumI
3807  01fd cd0000        	call	c_ltor
3809  0200 96            	ldw	x,sp
3810  0201 1c0001        	addw	x,#OFST-19
3811  0204 cd0000        	call	c_fdiv
3813  0207 be02          	ldw	x,c_lreg+2
3814  0209 89            	pushw	x
3815  020a be00          	ldw	x,c_lreg
3816  020c 89            	pushw	x
3817  020d cd0000        	call	_sqrt
3819  0210 5b04          	addw	sp,#4
3820  0212 96            	ldw	x,sp
3821  0213 1c000d        	addw	x,#OFST-7
3822  0216 cd0000        	call	c_fmul
3824  0219 ae0036        	ldw	x,#_Irms
3825  021c cd0000        	call	c_rtol
3827                     ; 118   realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
3829  021f 1e13          	ldw	x,(OFST-1,sp)
3830  0221 cd0000        	call	c_uitof
3832  0224 96            	ldw	x,sp
3833  0225 1c0001        	addw	x,#OFST-19
3834  0228 cd0000        	call	c_rtol
3836  022b 96            	ldw	x,sp
3837  022c 1c0009        	addw	x,#OFST-11
3838  022f cd0000        	call	c_ltor
3840  0232 96            	ldw	x,sp
3841  0233 1c000d        	addw	x,#OFST-7
3842  0236 cd0000        	call	c_fmul
3844  0239 ae0002        	ldw	x,#_sumP
3845  023c cd0000        	call	c_fmul
3847  023f 96            	ldw	x,sp
3848  0240 1c0001        	addw	x,#OFST-19
3849  0243 cd0000        	call	c_fdiv
3851  0246 ae0046        	ldw	x,#_realPower
3852  0249 cd0000        	call	c_rtol
3854                     ; 119   apparentPower = Vrms * Irms;
3856  024c ae003a        	ldw	x,#_Vrms
3857  024f cd0000        	call	c_ltor
3859  0252 ae0036        	ldw	x,#_Irms
3860  0255 cd0000        	call	c_fmul
3862  0258 ae0042        	ldw	x,#_apparentPower
3863  025b cd0000        	call	c_rtol
3865                     ; 121 }
3868  025e 5b16          	addw	sp,#22
3869  0260 81            	ret
3893                     ; 123 float getRealPower()
3893                     ; 124 {
3894                     	switch	.text
3895  0261               _getRealPower:
3899                     ; 125 	return realPower;
3901  0261 ae0046        	ldw	x,#_realPower
3902  0264 cd0000        	call	c_ltor
3906  0267 81            	ret
3930                     ; 128 float getApparentPower()
3930                     ; 129 {
3931                     	switch	.text
3932  0268               _getApparentPower:
3936                     ; 130 	return apparentPower;
3938  0268 ae0042        	ldw	x,#_apparentPower
3939  026b cd0000        	call	c_ltor
3943  026e 81            	ret
3967                     ; 133 float getVrms()
3967                     ; 134 {
3968                     	switch	.text
3969  026f               _getVrms:
3973                     ; 135 	return Vrms;
3975  026f ae003a        	ldw	x,#_Vrms
3976  0272 cd0000        	call	c_ltor
3980  0275 81            	ret
4213                     .bit:	section	.data,bit
4214  0000               _checkVCross:
4215  0000 00            	ds.b	1
4216                     	xdef	_checkVCross
4217  0001               _lastVCross:
4218  0001 00            	ds.b	1
4219                     	xdef	_lastVCross
4220                     	switch	.ubsct
4221  0000               _startV:
4222  0000 0000          	ds.b	2
4223                     	xdef	_startV
4224  0002               _sumP:
4225  0002 00000000      	ds.b	4
4226                     	xdef	_sumP
4227  0006               _instP:
4228  0006 00000000      	ds.b	4
4229                     	xdef	_instP
4230  000a               _sumI:
4231  000a 00000000      	ds.b	4
4232                     	xdef	_sumI
4233  000e               _sumV:
4234  000e 00000000      	ds.b	4
4235                     	xdef	_sumV
4236  0012               _phaseShiftedV:
4237  0012 00000000      	ds.b	4
4238                     	xdef	_phaseShiftedV
4239  0016               _offsetI:
4240  0016 00000000      	ds.b	4
4241                     	xdef	_offsetI
4242  001a               _offsetV:
4243  001a 00000000      	ds.b	4
4244                     	xdef	_offsetV
4245  001e               _filteredI:
4246  001e 00000000      	ds.b	4
4247                     	xdef	_filteredI
4248  0022               _filteredV:
4249  0022 00000000      	ds.b	4
4250                     	xdef	_filteredV
4251  0026               _lastFilteredV:
4252  0026 00000000      	ds.b	4
4253                     	xdef	_lastFilteredV
4254  002a               _ICAL:
4255  002a 00000000      	ds.b	4
4256                     	xdef	_ICAL
4257  002e               _VCAL:
4258  002e 00000000      	ds.b	4
4259                     	xdef	_VCAL
4260  0032               _sampleI:
4261  0032 0000          	ds.b	2
4262                     	xdef	_sampleI
4263  0034               _sampleV:
4264  0034 0000          	ds.b	2
4265                     	xdef	_sampleV
4266  0036               _Irms:
4267  0036 00000000      	ds.b	4
4268                     	xdef	_Irms
4269  003a               _Vrms:
4270  003a 00000000      	ds.b	4
4271                     	xdef	_Vrms
4272  003e               _powerFactor:
4273  003e 00000000      	ds.b	4
4274                     	xdef	_powerFactor
4275  0042               _apparentPower:
4276  0042 00000000      	ds.b	4
4277                     	xdef	_apparentPower
4278  0046               _realPower:
4279  0046 00000000      	ds.b	4
4280                     	xdef	_realPower
4281                     	xdef	_getVrms
4282                     	xdef	_getRealPower
4283                     	xdef	_getApparentPower
4284                     	xdef	_calcVI
4285                     	xdef	_PHASECAL
4286                     	xdef	_ADC_COUNTS
4287                     	xref	_readChannel
4288                     	xref	_sqrt
4289                     	switch	.const
4290  0006               L7542:
4291  0006 45800000      	dc.w	17792,0
4292  000a               L3442:
4293  000a 44800000      	dc.w	17536,0
4294  000e               L5242:
4295  000e 44e66666      	dc.w	17638,26214
4296  0012               L5142:
4297  0012 450ccccc      	dc.w	17676,-13108
4298  0016               L7732:
4299  0016 43520000      	dc.w	17234,0
4300  001a               L7632:
4301  001a 40533333      	dc.w	16467,13107
4302                     	xref.b	c_lreg
4303                     	xref.b	c_x
4323                     	xref	c_uitof
4324                     	xref	c_fadd
4325                     	xref	c_fmul
4326                     	xref	c_ltor
4327                     	xref	c_fgadd
4328                     	xref	c_fdiv
4329                     	xref	c_fsub
4330                     	xref	c_fcmp
4331                     	xref	c_itof
4332                     	xref	c_rtol
4333                     	xref	c_ctof
4334                     	end
