import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import logging
from dotenv import load_dotenv

# Import our custom modules
from data_collector import LetterboxdDataCollector
from dataset_builder import DatasetBuilder
from model_trainer import ModelTrainer
from recommender import RecommendationEngine
from database import DatabaseManager

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Initialize components
db_manager = DatabaseManager()
data_collector = LetterboxdDataCollector(db_manager)
dataset_builder = DatasetBuilder(db_manager)
model_trainer = ModelTrainer(db_manager)
recommendation_engine = RecommendationEngine(db_manager)

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'movie-recommender-ml',
        'version': '1.0.0'
    })

@app.route('/data/collect/<username>', methods=['POST'])
def collect_user_data(username):
    """Collect data for a single Letterboxd user"""
    try:
        logger.info(f"Starting data collection for user: {username}")
        
        result = data_collector.collect_user_data(username)
        
        # Update dataset metrics after collection
        dataset_builder.update_dataset_metrics()
        
        return jsonify({
            'status': 'success',
            'message': f'Data collected for user {username}',
            'data': result
        })
        
    except Exception as e:
        logger.error(f"Error collecting data for user {username}: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/data/bulk-collect', methods=['POST'])
def bulk_collect_data():
    """Collect data for multiple Letterboxd users"""
    try:
        data = request.get_json()
        usernames = data.get('usernames', [])
        
        if not usernames:
            return jsonify({
                'status': 'error',
                'message': 'No usernames provided'
            }), 400
        
        logger.info(f"Starting bulk data collection for {len(usernames)} users")
        
        results = data_collector.bulk_collect_users(usernames)
        
        # Update dataset metrics after bulk collection
        dataset_builder.update_dataset_metrics()
        
        return jsonify({
            'status': 'success',
            'message': f'Bulk data collection completed for {len(usernames)} users',
            'results': results
        })
        
    except Exception as e:
        logger.error(f"Error during bulk data collection: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/data/stats', methods=['GET'])
def get_dataset_stats():
    """Get dataset statistics and health metrics"""
    try:
        stats = dataset_builder.get_dataset_statistics()
        return jsonify({
            'status': 'success',
            'stats': stats
        })
        
    except Exception as e:
        logger.error(f"Error getting dataset stats: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/model/train', methods=['POST'])
def train_model():
    """Trigger model training on current dataset"""
    try:
        logger.info("Starting model training")
        
        # Check if dataset is ready for training
        if not dataset_builder.is_ready_for_training():
            return jsonify({
                'status': 'error',
                'message': 'Dataset not ready for training'
            }), 400
        
        # Build training dataset
        dataset_builder.prepare_training_data()
        
        # Train models
        training_results = model_trainer.train_models()
        
        return jsonify({
            'status': 'success',
            'message': 'Model training completed',
            'results': training_results
        })
        
    except Exception as e:
        logger.error(f"Error during model training: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/model/status', methods=['GET'])
def get_training_status():
    """Get training progress and model info"""
    try:
        status = model_trainer.get_training_status()
        return jsonify({
            'status': 'success',
            'training_status': status
        })
        
    except Exception as e:
        logger.error(f"Error getting training status: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/recommend/user/<int:user_id>', methods=['POST'])
def get_user_recommendations(user_id):
    """Get recommendations for a specific user"""
    try:
        logger.info(f"Generating recommendations for user: {user_id}")
        
        recommendations = recommendation_engine.get_recommendations_for_user(user_id)
        
        return jsonify({
            'status': 'success',
            'user_id': user_id,
            'recommendations': recommendations,
            'algorithm': recommendation_engine.get_current_algorithm(),
            'model_version': recommendation_engine.get_model_version()
        })
        
    except Exception as e:
        logger.error(f"Error generating recommendations for user {user_id}: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/recommend/cold-start', methods=['POST'])
def get_cold_start_recommendations():
    """Get recommendations for new users (cold start)"""
    try:
        data = request.get_json()
        user_id = data.get('user_id')
        preferred_genres = data.get('preferred_genres', [])
        
        logger.info(f"Generating cold start recommendations for user: {user_id}")
        
        recommendations = recommendation_engine.get_cold_start_recommendations(
            user_id, preferred_genres
        )
        
        return jsonify({
            'status': 'success',
            'user_id': user_id,
            'recommendations': recommendations,
            'algorithm': 'cold_start',
            'model_version': recommendation_engine.get_model_version()
        })
        
    except Exception as e:
        logger.error(f"Error generating cold start recommendations: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/dataset/export', methods=['GET'])
def export_dataset():
    """Export training data for analysis"""
    try:
        export_path = dataset_builder.export_dataset()
        
        return jsonify({
            'status': 'success',
            'message': 'Dataset exported successfully',
            'export_path': export_path
        })
        
    except Exception as e:
        logger.error(f"Error exporting dataset: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    debug = os.environ.get('FLASK_DEBUG', 'False').lower() == 'true'
    
    logger.info(f"Starting ML service on port {port}")
    app.run(host='0.0.0.0', port=port, debug=debug)