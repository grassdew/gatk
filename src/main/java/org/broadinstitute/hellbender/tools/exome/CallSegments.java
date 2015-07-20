package org.broadinstitute.hellbender.tools.exome;

import org.broadinstitute.hellbender.cmdline.Argument;
import org.broadinstitute.hellbender.cmdline.CommandLineProgram;
import org.broadinstitute.hellbender.cmdline.CommandLineProgramProperties;
import org.broadinstitute.hellbender.cmdline.StandardArgumentDefinitions;
import org.broadinstitute.hellbender.cmdline.programgroups.ExomeAnalysisProgramGroup;
import org.broadinstitute.hellbender.exceptions.UserException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Calls segments as amplified, deleted, or copy number neutral given files containing tangent-normalized
 * read counts by target and a list of segments
 *
 * @author David Benjamin
 */
@CommandLineProgramProperties(
        usage = "Call segments as amplified, deleted, or copy number neutral given files containing tangent-normalized" +
                " read counts by target and a list of segments",
        usageShort = "Call segments as amplified, deleted, or copy number neutral",
        programGroup = ExomeAnalysisProgramGroup.class
)
public final class CallSegments extends CommandLineProgram{

    protected final static String SEGFILE_SHORT_NAME = "S";
    protected final static String SEGFILE_LONG_NAME = "segments";

    protected final static String TARGET_FILE_SHORT_NAME = "T";
    protected final static String TARGET_FILE_LONG_NAME = "targets";

    protected final static String OUTPUT_SHORT_NAME = StandardArgumentDefinitions.OUTPUT_SHORT_NAME;
    protected final static String OUTPUT_LONG_NAME = StandardArgumentDefinitions.OUTPUT_LONG_NAME;

    @Argument(
            doc = "normalized read counts input file.",
            shortName = TARGET_FILE_SHORT_NAME,
            fullName = TARGET_FILE_LONG_NAME,
            optional = false
    )
    protected File targetsFile;

    @Argument(
            doc = "segments files",
            shortName = SEGFILE_SHORT_NAME,
            fullName = SEGFILE_LONG_NAME,
            optional = false
    )
    protected File segmentsFile;

    @Argument(
            doc = "Called segments output",
            shortName = OUTPUT_SHORT_NAME,
            fullName = OUTPUT_LONG_NAME,
            optional = false
    )
    protected File outFile;

    @Override
    protected Object doWork() {

        final List<Segment> segments;
        final List<TargetCoverage> targetList;

        try {
            targetList = TargetCoverageUtils.readTargetsWithCoverage(targetsFile);
        } catch (final IOException e) {
            throw new UserException.CouldNotReadInputFile(targetsFile, e);
        }

        final HashedListExonCollection<TargetCoverage> targets = new HashedListExonCollection<TargetCoverage>(targetList);

        try {
            segments = SegmentUtils.readUncalledSegments(segmentsFile);
        } catch (final IOException e) {
            throw new UserException.CouldNotReadInputFile(segmentsFile, e);
        }



        //add calls to segments in-place
        ReCapSegCaller.makeCalls(targets, segments);

        try {
            SegmentUtils.writeCalledSegments(outFile, segments);
        } catch (final IOException e) {
            throw new UserException.CouldNotCreateOutputFile(outFile, e);
        }

        return "SUCCESS";
    }
}
