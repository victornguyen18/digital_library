import logging
from rs_system.builder.book_similarity_calculator import CalculateItemSimilarity
from rs_system.builder.point_calculator import CalculatePointAllUser

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger("Import initial database")

logger.info("Run train data")
CalculatePointAllUser().calculate(run_force=True)
CalculateItemSimilarity().calculate(run_force=True)
