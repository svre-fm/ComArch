        add  0 0 1        # $1 = 0 (Initialize result to 0)
        LW   0 2 mcand    # $2 = Load mcand
        lw   0 3 mplier   # $3 = Load mplier
        lw   0 4 pos1     # $4 = Mask initialized to 1
        lw   0 5 limit    # $5 = Loop limit (65536 / 2^16)
        lw   0 6 neg1     # $6 = Constant -1 for checking NAND result

loop    beq  4 5 done     # If Mask ($4) == Limit ($5), exit loop
        nand 3 4 7        # $7 = mplier NAND Mask
        beq  7 6 skip     # If $7 == -1, current bit is 0 (skip addition)
        Add  1 2 1        # Current bit is 1: accumulate mcand ($2) into result ($1)

skip    add  2 2 2        # mcand = mcand * 2 (Shift Left 1 bit)
        add  4 4 4        # Mask = Mask * 2 (Shift Left 1 bit)
        beq  0 0 loop     # Jump back to loop head

done    halt              # End program (Result in $1)

pos1    .fill 1
limit   .fill 65536
neg1    .fill -1
mcand   .fill 32766       # Test input multiplicand
mplier  .fill 10383       # Test input multiplier