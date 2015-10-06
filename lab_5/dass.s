	.file	"dass.cc"
	.section	".toc","aw"
	.section	".text"
	.section	.text._ZNSt13__atomic_baseIyEC1Ev,"axG",@progbits,_ZNSt13__atomic_baseIyEC1Ev,comdat
	.align 2
	.weak	_ZNSt13__atomic_baseIyEC1Ev
	.section	".opd","aw"
	.align 3
_ZNSt13__atomic_baseIyEC1Ev:
	.quad	.L._ZNSt13__atomic_baseIyEC1Ev,.TOC.@tocbase
	.previous
	.type	_ZNSt13__atomic_baseIyEC1Ev, @function
.L._ZNSt13__atomic_baseIyEC1Ev:
.LFB328:
	.cfi_startproc
	std 31,-8(1)
	stdu 1,-64(1)
	.cfi_def_cfa_offset 64
	.cfi_offset 31, -8
	mr 31,1
	.cfi_def_cfa_register 31
	std 3,112(31)
	addi 1,31,64
	.cfi_def_cfa 1, 0
	ld 31,-8(1)
	blr
	.long 0
	.byte 0,9,0,0,128,1,0,1
	.cfi_endproc
.LFE328:
	.size	_ZNSt13__atomic_baseIyEC1Ev,.-.L._ZNSt13__atomic_baseIyEC1Ev
	.lcomm	_ZL3sum,8,8
	.type	_ZL3sum, @object
	.section	".text"
	.align 2
	.section	".opd","aw"
	.align 3
_ZL7consumev:
	.quad	.L._ZL7consumev,.TOC.@tocbase
	.previous
	.type	_ZL7consumev, @function
.L._ZL7consumev:
.LFB329:
	.cfi_startproc
	mflr 0
	std 0,16(1)
	std 31,-8(1)
	stdu 1,-144(1)
	.cfi_def_cfa_offset 144
	.cfi_offset 65, 16
	.cfi_offset 31, -8
	mr 31,1
	.cfi_def_cfa_register 31
	addis 3,2,_ZL3sum@toc@ha
	addi 3,3,_ZL3sum@toc@l
	ld 4,112(31)
	bl _ZNSt13__atomic_baseIyEpLEy
	nop
	addi 1,31,144
	.cfi_def_cfa 1, 0
	ld 0,16(1)
	mtlr 0
	ld 31,-8(1)
	blr
	.long 0
	.byte 0,9,0,1,128,1,0,1
	.cfi_endproc
.LFE329:
	.size	_ZL7consumev,.-.L._ZL7consumev
	.section	.text._ZNSt13__atomic_baseIyEpLEy,"axG",@progbits,_ZNSt13__atomic_baseIyEpLEy,comdat
	.align 2
	.weak	_ZNSt13__atomic_baseIyEpLEy
	.section	".opd","aw"
	.align 3
_ZNSt13__atomic_baseIyEpLEy:
	.quad	.L._ZNSt13__atomic_baseIyEpLEy,.TOC.@tocbase
	.previous
	.type	_ZNSt13__atomic_baseIyEpLEy, @function
.L._ZNSt13__atomic_baseIyEpLEy:
.LFB348:
	.cfi_startproc
	std 31,-8(1)
	stdu 1,-64(1)
	.cfi_def_cfa_offset 64
	.cfi_offset 31, -8
	mr 31,1
	.cfi_def_cfa_register 31
	std 3,112(31)
	std 4,120(31)
	ld 10,112(31)
	ld 8,120(31)
	sync
.L4:
	ldarx 9,0,10
	add 9,9,8
	stdcx. 9,0,10
	mcrf 7,0
	bne 7,.L4
	isync
	mr 3,9
	addi 1,31,64
	.cfi_def_cfa 1, 0
	ld 31,-8(1)
	blr
	.long 0
	.byte 0,9,0,0,128,1,0,1
	.cfi_endproc
.LFE348:
	.size	_ZNSt13__atomic_baseIyEpLEy,.-.L._ZNSt13__atomic_baseIyEpLEy
	.section	".text"
	.align 2
	.section	".opd","aw"
	.align 3
_Z41__static_initialization_and_destruction_0ii:
	.quad	.L._Z41__static_initialization_and_destruction_0ii,.TOC.@tocbase
	.previous
	.type	_Z41__static_initialization_and_destruction_0ii, @function
.L._Z41__static_initialization_and_destruction_0ii:
.LFB349:
	.cfi_startproc
	mflr 0
	std 0,16(1)
	std 31,-8(1)
	stdu 1,-128(1)
	.cfi_def_cfa_offset 128
	.cfi_offset 65, 16
	.cfi_offset 31, -8
	mr 31,1
	.cfi_def_cfa_register 31
	mr 10,3
	mr 9,4
	stw 10,176(31)
	stw 9,184(31)
	lwz 9,176(31)
	cmpwi 7,9,1
	bne 7,.L6
	lwz 10,184(31)
	li 9,0
	ori 9,9,65535
	cmpw 7,10,9
	bne 7,.L6
	addis 3,2,_ZL3sum@toc@ha
	addi 3,3,_ZL3sum@toc@l
	bl _ZNSt13__atomic_baseIyEC1Ev
	nop
.L6:
	addi 1,31,128
	.cfi_def_cfa 1, 0
	ld 0,16(1)
	mtlr 0
	ld 31,-8(1)
	blr
	.long 0
	.byte 0,9,0,1,128,1,0,1
	.cfi_endproc
.LFE349:
	.size	_Z41__static_initialization_and_destruction_0ii,.-.L._Z41__static_initialization_and_destruction_0ii
	.align 2
	.section	".opd","aw"
	.align 3
_GLOBAL__sub_I_dass.cc:
	.quad	.L._GLOBAL__sub_I_dass.cc,.TOC.@tocbase
	.previous
	.type	_GLOBAL__sub_I_dass.cc, @function
.L._GLOBAL__sub_I_dass.cc:
.LFB350:
	.cfi_startproc
	mflr 0
	std 0,16(1)
	std 31,-8(1)
	stdu 1,-128(1)
	.cfi_def_cfa_offset 128
	.cfi_offset 65, 16
	.cfi_offset 31, -8
	mr 31,1
	.cfi_def_cfa_register 31
	li 3,1
	li 4,-1
	rldicl 4,4,0,48
	bl _Z41__static_initialization_and_destruction_0ii
	addi 1,31,128
	.cfi_def_cfa 1, 0
	ld 0,16(1)
	mtlr 0
	ld 31,-8(1)
	blr
	.long 0
	.byte 0,9,0,1,128,1,0,1
	.cfi_endproc
.LFE350:
	.size	_GLOBAL__sub_I_dass.cc,.-.L._GLOBAL__sub_I_dass.cc
	.section	.init_array,"aw"
	.align 3
	.quad	_GLOBAL__sub_I_dass.cc
	.ident	"GCC: (GNU) 4.8.2 20131212 (Red Hat 4.8.2-7)"
	.section	.note.GNU-stack,"",@progbits
