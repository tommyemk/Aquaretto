package service

import edu.udo.cs.sopra.ntf.*
import entity.Waterpark
import kotlin.test.*


/**
 * Tests the coordinate conversion functions
 */
class NetworkServiceCoordinateConversionTest {
    private val test1Local = PositionPair(8, 8)
    private val test2Local = PositionPair(0, 0)
    private val test3Local = PositionPair(10, 8)
    private val test4Local = PositionPair(12, 15)
    private val test5Local = Waterpark.DEPOT_POS.toPos()
    private val test1Network = PositionPair(0, 4)
    private val test2Network = PositionPair(-8, 12)
    private val test3Network = PositionPair(2, 4)
    private val test4Network = PositionPair(4, -3)
    private val test5Network = PositionPair(0, 0)

    /**
     * Tests local coordinate to network coordinate
     */
    @Test
    fun localToNetworkTest() {
        assertEquals(test1Network, NetworkService.localToNetworkCoordinate(test1Local))
        assertEquals(test2Network, NetworkService.localToNetworkCoordinate(test2Local))
        assertEquals(test3Network, NetworkService.localToNetworkCoordinate(test3Local))
        assertEquals(test4Network, NetworkService.localToNetworkCoordinate(test4Local))
        assertEquals(test5Network, NetworkService.localToNetworkCoordinate(test5Local))
    }

    /**
     * Tests network coordinate to local coordinate
     */
    @Test
    fun networkToLocalTest() {
        assertEquals(test1Local, NetworkService.networkToLocalCoordinate(test1Network))
        assertEquals(test2Local, NetworkService.networkToLocalCoordinate(test2Network))
        assertEquals(test3Local, NetworkService.networkToLocalCoordinate(test3Network))
        assertEquals(test4Local, NetworkService.networkToLocalCoordinate(test4Network))
        assertEquals(test5Local, NetworkService.networkToLocalCoordinate(test5Network))
    }

    /**
     * Test helper methods for coordinates
     */
    @Test
    fun helperMethodsTest() {
        assertEquals(JobEnum.KEEPER, Waterpark.ANIM_POS_1.toWorkerTriple().jobEnum)
        assertEquals(JobEnum.KEEPER, Waterpark.ANIM_POS_2.toWorkerTriple().jobEnum)
        assertEquals(JobEnum.MANAGER, Waterpark.DEPOT_POS.toWorkerTriple().jobEnum)
        assertEquals(JobEnum.CASHIER, Waterpark.CASH_POS_1.toWorkerTriple().jobEnum)
        assertEquals(JobEnum.CASHIER, Waterpark.CASH_POS_2.toWorkerTriple().jobEnum)
        assertEquals(WorkerTriple(0, 2, JobEnum.TRAINER), Pair(8, 10).toWorkerTriple())

        assertEquals(Pair(1,5), PositionPair(1, 5).toPair())
        assertEquals(PositionPair(6,2), AnimalTriple(6, 2, 0).getPos())
        assertEquals(PositionPair(8,3), OffspringTriple(8, 3, 50).getPos())
        assertEquals(PositionPair(12,4), WorkerTriple(12, 4, JobEnum.TRAINER).getPos())

        assertEquals(PositionPair(12, 25), PositionPair(10, 20) + PositionPair(2, 5))
        assertEquals(PositionPair(8, 15), PositionPair(10, 20) - PositionPair(2, 5))
    }
}